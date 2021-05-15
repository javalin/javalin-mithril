@package io.javalin.test;
@import io.javalin.test.ImportComponent;
@import io.javalin.test2.SimpleComponent;

class NameSpaceComponent{
    constructor(){
        new ImportComponent();
        new SimpleComponent();
    }
}
