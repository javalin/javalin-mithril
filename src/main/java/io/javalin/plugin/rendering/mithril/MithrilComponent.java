/*
 *  Copyright 2021 Tareq Kirresh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.javalin.plugin.rendering.mithril;

import io.javalin.core.util.Header;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.plugin.json.JsonMapper;
import static io.javalin.plugin.json.JsonMapperKt.JSON_MAPPER_KEY;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author tareq
 */
public class MithrilComponent implements Handler {

    private final String componentFullClassName;
    private final Function<Context, Map> localStateFunction;

    /**
     * Routes to a Mithril component with the Fully Qualified Class Name
     *
     * @param componentFullClassName the FQCN of the component
     */
    public MithrilComponent(String componentFullClassName) {
        this.componentFullClassName = componentFullClassName.replaceAll("\\.", "_");
        this.localStateFunction = null;
    }

    /**
     * Routes to a Mithril component with the Fully Qualified Class Name and a
     * Local State Function
     *
     * @param componentFullClassName the FQCN of the component
     * @param localStateFunction the state function
     */
    public MithrilComponent(String componentFullClassName, Function<Context, Map> localStateFunction) {
        this.componentFullClassName = componentFullClassName.replaceAll("\\.", "_");
        this.localStateFunction = localStateFunction;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            String page = JavalinMithril.layoutPage()
                    .replace("@routeComponent", String.format("m(%s)", componentFullClassName))
                    .replace("@componentRegistration", "<script>\n@serverState\n@componentRegistration\n</script>")
                    .replace("@componentRegistration", JavalinMithril.resolver().resolve(componentFullClassName))
                    .replace("@serverState", state(ctx))
                    .replace("@cdnWebjar/", JavalinMithril.isDev() ? "/webjars/" : "https://cdn.jsdelivr.net/webjars/org.webjars.npm/");
            ctx.html(page).header(Header.CACHE_CONTROL, JavalinMithril.cacheControl);
        } catch (Exception ex) {
            throw new InternalServerErrorResponse(String.format("%s : %s ", ex.getClass().getName(),ex.getMessage()));
        }
    }

    private String state(Context ctx) {
        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put("pathParams", ctx.pathParamMap());
        stateMap.put("queryParams", ctx.queryParamMap().entrySet()
                .stream()
                .map(entry -> new SimpleImmutableEntry(entry.getKey(), (entry.getValue() == null || entry.getValue().size() > 1) ? entry.getValue() : entry.getValue().get(0)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue))
        );
        Map componentState = new HashMap<>();
        Map globalState = JavalinMithril.stateFunction.apply(ctx);
        componentState.putAll(globalState);
        if (this.localStateFunction != null) {
            componentState.putAll(this.localStateFunction.apply(ctx));
        }
        stateMap.put("state", componentState);
        String stateString =  ((JsonMapper)ctx.appAttribute(JSON_MAPPER_KEY)).toJsonString(stateMap);
        return String.format("window.javalin = %s", stateString);
    }

}
