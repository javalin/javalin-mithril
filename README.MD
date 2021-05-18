![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.javalin/javalinmithril/badge.png)
# JavalinMithril
## Introduction
This is the [Mithril.js](https://mithril.js.org/) plugin for [Javalin](https://javalin.io). It allows
you to use Mithril.js and Server Side Routing and State Injection to create multi-page applications
with javalin. It is heavily inspired by the JavalinVue plugin.
## Usage
### Including in your project
Add the following to your ```pom.xml```

```xml
<dependency>
    <groupId>io.github.javalin</groupId>
    <artifactId>javalinmithril</artifactId>
    <version>${LATEST_VERSION}</version>
</dependency>
```

You will also need the webjar for Mithril.js

```xml
<dependency>
    <groupId>org.webjars.npm</groupId>
    <artifactId>mithril</artifactId>
    <version>2.0.4</version>
</dependency>
```
### Folder Structure
The plugin looks for mithril JavaScript class files in ```/src/main/resources/mithril``` by default.
The layout of the page is in ```layout.html```, and the components are in any ```*.js``` files in the directory
or its children.
```
root
   |_src
     |_main
        |_resources
        |_mithril
          |_layout.html
          |_component1.js
          |_component2.js
          |_comonent100.js  
```
### Configuration
You need to start by enabling webjars. this is done using ```app.config.enableWebjars();```.
 Afterwards, Configuration is done using a function that consumes the configuration object. You can 
set the path to the mithril root directory, the state function which will be injected to 
every request, the running mode(dev or not), and the Cache Header Values.
```java
app.config.enableWebjars();
JavalinMithril.configure(config -> {
    config.isDev(true)
        .stateFunction(ctx -> singletonMap("test", "var"))
        .filePath("src/test/resources/mithril");
});

```

Additionally, you will need a ```layout.html``` file which looks something like this

```html
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf8"/>
        <link rel="stylesheet" href="/webjars/font-awesome/5.14.0/css/all.min.css"></link>
        <script src="/webjars/mithril/2.0.4/mithril.min.js"></script>
        @componentRegistration
    <body>
        <main id="main-view" class="app">
        </main>
        <script>
           m.render(document.getElementById("main-view"), @routeComponent)
        </script>
    </body>
</html>

```

The key is in the ```@componentRegistration``` and ```@routeComponent``` directives. Addionally,
there is an ```@cdnwebjar``` directive which you can use instead of just ```/webjars``` to use a 
CDN when in non-dev mode. In the case above, this would look like this :

```html
<link rel="stylesheet" href="@cdnwebjar/font-awesome/5.14.0/css/all.min.css"></link>
<script src="@cdnwebjar/mithril/2.0.4/mithril.min.js"></script>

```
### Structure of JS Files
Each js file must contain an  ```@package my.package.name;``` in the top of the file
followed optionally with a set of ```@import my.package.name.MyDependencyClass;``` directives.
These are used by JavalinMithril to resolve dependencies between mithril files in the project
to reduce the size of delivered File. The JS File would then look like this

```js
@package io.javalin.test;
@import io.javalin.test.SingleComponent;


class ImportComponent2{
    constructor(){
        this.singleComponent = new SingleComponent();
    }

    view(){
        return m("div","Hello World")
    }
}


```

This is not a tutorial on how to use mithril.js, so you will have to see how 
class-based components work there.

### Calling a Component
To call a component in your javalin app, you need its Fully Qualified Class Name. 

```java

app.get("/my-page",new MithrilComponent("io.javalin.test.ImportComponent2"));

```

You can use roles like any other endpoint, and before/after filters apply as well.
Additionally, you can have a per-component state function, which overrides the values
in the global state function if they have the same keys

```java

app.get("/my-page",new MithrilComponent("io.javalin.test.ImportComponent2",ctx->{/*Function that returns map*/}));

```

You can look in the ```src/test``` directory on more config examples and usages.

## Contributing & Developing
The project is a simple maven project. All you need is any IDE capable of working with ```pom.xml``` files
and you are good to go. Pull Requests and Feature Requests are more than welcome - there are no specific 
formats or processes in place.

## Special Notes
Thanks to [David Aase](https://github.com/tipsy/), for creating Javalin, and being open to new ideas 
and directions in the framework.