@package io.javalin.test;
@import io.javalin.test.ImportComponent;
@import io.javalin.test2.SingleComponent;

class NameSpaceComponent{
    constructor(){
        new ImportComponent();
        new SingleComponent();
    }
}
