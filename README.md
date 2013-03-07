Key = value aligner
===============

IDEA Plugin for align key = value pair

```scala
// before
val test = "Hello world!"
val mySuperValue = 123
val testActor = actorSystem.actorFor(Props(new Producer))

// after
val test         = "Hello world!"
val mySuperValue = 123
val testActor    = actorSystem.actorFor(Props(new Producer))
```


Imports beautifier
==================

Rearrange imports for grouping by lexical scope.
Imports in each group sorted alphabetically.

Before: 
```scala
package ru.kulikovd.myproject.module

import com.rabbitmq.client.Envelope
import com.typesafe.config.Config
import grizzled.slf4j.Logging
import ru.kulikovd.common.util.Time
import ru.kulikovd.myproject.config.Factory
import scala.collection.JavaConversions._
import ru.kulikovd.myproject.core.Registry
import ru.kulikovd.myproject.Settings
import ru.kulikovd.myproject.module.Repository
import ru.kulikovd.othermodule._
import scala.collection.mutable.{Map ⇒ MutableMap}
import java.util.Date
```

After:
```scala
package ru.kulikovd.myproject.module

import java.util.Date // 1. java imports group

import scala.collection.JavaConversions._ // 2. scala 
import scala.collection.mutable.{Map ⇒ MutableMap}

import com.rabbitmq.client.Envelope // 3. third party libraries
import com.typesafe.config.Config
import grizzled.slf4j.Logging

import ru.kulikovd.common.util.Time // 3. other modules from own project
import ru.kulikovd.othermodule._

import ru.kulikovd.myproject.Settings
import ru.kulikovd.myproject.config.Factory
import ru.kulikovd.myproject.core.Registry
import ru.kulikovd.myproject.module.Repository // 4. local imports in current module
```
