/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.javalinmithril;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 *
 * @author tareq
 */
public class MithrilComponent implements Handler {

    private String componentFullClassName;
    private String indexPage;

    public MithrilComponent(String componentFullClassName) {
        this.componentFullClassName = componentFullClassName.replaceAll(".", "_");
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String state = getState();
        String component = JavalinMithril.resolver.resolve(componentFullClassName);
        String page = indexPage.replaceAll("@routeComponent", String.format("new %s()", componentFullClassName))
                .replaceAll("@componentRegistration", "@componentRegistration\n@stateRegistration")
                .replaceAll("@stateRegistration", state)
                .replaceAll("@componentRegistration", component);

        ctx.html(page);
    }

    private String getState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
