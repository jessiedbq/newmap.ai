package ai.newmap.model

import scala.collection.mutable.StringBuilder

// TODO: will these also have (optionally?) a hash value
case class EnvironmentCommand(
  id: String,
  nType: NewMapType,
  nObject: NewMapObject
) {
  override def toString: String = {
    "val " + id + ": " + nType + " = " + nObject
  }
}

case class ObjectWithType(
  nType: NewMapType,
  nObject: NewMapObject
)

case class Environment(commands: Vector[EnvironmentCommand]) {
  def typeOf(
    identifier: String
  ): Option[NewMapType] = {
    commands.reverse.find(_.id == identifier).map(_.nType)
  }

  def objectOf(
    identifier: String
  ): Option[NewMapObject] = {
    commands.reverse.find(_.id == identifier).map(_.nObject)
  }

  def lookup(identifier: String): Option[ObjectWithType] = {
    val resultOpt = commands.reverse.find(_.id == identifier)
    resultOpt.map(result => {
      ObjectWithType(result.nType, result.nObject)
    })
  }

  override def toString: String = {
    val builder: StringBuilder = new StringBuilder()
    for (command <- commands) {
      builder.append(command.toString)
      builder.append("\n")
    }
    builder.toString
  }

  def print(): Unit = {
    for {
      command <- commands
    } {
      println(command.toString)
    }
  }

  def newCommand(command: EnvironmentCommand): Environment = {
    Environment(commands :+ command)
  }

  def newCommands(newCommands: Vector[EnvironmentCommand]): Environment = {
    Environment(commands ++ newCommands)
  }

  def newParam(id: String, nType: NewMapType): Environment = {
    Environment(commands :+ Environment.paramToEnvCommand((id, nType)))
  }

  def newParams(xs: Vector[(String, NewMapType)]) = {
    Environment(commands ++ xs.map(Environment.paramToEnvCommand))
  }
}

object Environment {
  val Base: Environment = Environment(Vector(
    EnvironmentCommand("Object", TypeT, ObjectType),
    EnvironmentCommand("Type", TypeT, TypeType),
    EnvironmentCommand("Identifier", TypeT, IdentifierType),
    EnvironmentCommand("Map", LambdaT(
      params = Vector(
        "key" -> TypeT,
        "value" -> TypeT,
        "default" -> SubstitutableT("value")
      ),
      result = TypeT
    ), LambdaInstance(
      params = Vector(
        "key" -> TypeType,
        "value" -> TypeType,
        "default" -> ParameterObj("value")
      ),
      expression = MapType(
        ParameterObj("key"),
        ParameterObj("value"),
        ParameterObj("default")
      )
    )),    
    EnvironmentCommand("Struct", LambdaT(
      params = Vector(
        "params" -> MapT(IdentifierT, TypeT, Index(1))
      ),
      result = TypeT
    ), LambdaInstance(
      params = Vector(
        "params" -> MapType(IdentifierType, TypeType, Index(1))
      ),
      expression = StructType(
        ParameterObj("params")
      )
    ))
  ))

  def paramToEnvCommand(x: (String, NewMapType)): EnvironmentCommand = {
    EnvironmentCommand(x._1, x._2, ParameterObj(x._1))
  }
}
