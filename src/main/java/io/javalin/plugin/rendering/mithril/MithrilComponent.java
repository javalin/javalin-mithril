/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.javalin.plugin.rendering.mithril;

import io.javalin.core.util.Header;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.plugin.json.JavalinJson;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author tareq
 */
public class MithrilComponent implements Handler {

    private final String componentFullClassName;
    private final Function<Context, Map> localStateFunction;

    public MithrilComponent(String componentFullClassName) {
        this.componentFullClassName = componentFullClassName.replaceAll("\\.", "_");
        this.localStateFunction = null;
    }

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
            throw new InternalServerErrorResponse(ex.getMessage());
        }
    }

    private String state(Context ctx) {
        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put("pathParams", ctx.pathParamMap());
        stateMap.put("queryParams", ctx.queryParamMap());
        Map componentState = new HashMap<>();
        Map globalState = JavalinMithril.stateFunction.apply(ctx);
        componentState.putAll(globalState);
        if (this.localStateFunction != null) {
            componentState.putAll(this.localStateFunction.apply(ctx));
        }
        stateMap.put("state", componentState);
        String stateString = JavalinJson.toJson(stateMap);
        return String.format("window.javalin = %s", stateString);
    }

}
