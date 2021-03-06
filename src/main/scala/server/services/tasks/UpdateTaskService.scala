package server.services.tasks

import bounded_contexts.tasks.domain.TaskEntity
import cats.effect.IO
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.io._
import org.http4s.circe.jsonOf
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import bounded_contexts.tasks.application.UpdateTaskApplicationService
import bounded_contexts.tasks.application.FindTaskApplicationService
import server.services.helpers.Message

class UpdateTaskService {
  implicit val decoder = jsonOf[IO, TaskEntity]

  val service = HttpRoutes.of[IO] {
    case req @ PUT -> Root / "tasks" / id =>
      FindTaskApplicationService.find(id.toInt).flatMap { taskOption =>
        taskOption match  {
          case None => NotFound(Message("Not found").asJson)
          case Some(taskValue) => updateTask(req, taskValue)
        }
      }
  }

  def updateTask(request: Request[IO], taskValue: TaskEntity): IO[Response[IO]] = for {
    taskRequest <- request.as[TaskEntity].map { taskWithoutId =>
      taskWithoutId.copy(id = taskValue.id)
    }
    taskUpdated <- UpdateTaskApplicationService.update(taskRequest)
    response <- Ok(taskUpdated.asJson)
  } yield response
}
