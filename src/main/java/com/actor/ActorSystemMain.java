package com.actor;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.actions.RestartRaft;
import com.actions.StartRaft;
import com.actions.StopRaft;
import com.actions.Trigger;
import com.actor.ActorMain.MainActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;

public class ActorSystemMain {

	public static void main(String[] args) {
		ActorSystem actorSystem = ActorSystem.create("MyActorSystem-system");
		
		ActorRef ActorMain = actorSystem.actorOf(Props.create(MainActor.class));
		
		ActorMain.tell(new Trigger(), ActorRef.noSender());
		ActorMain.tell(new StartRaft("Starting.."), ActorRef.noSender());
		
		actorSystem.scheduler().scheduleOnce(FiniteDuration.apply(35000 + new Random().nextInt(10), TimeUnit.MILLISECONDS),
				ActorMain, new RestartRaft(), actorSystem.dispatcher(), ActorRef.noSender());
		
		actorSystem.scheduler().scheduleOnce(FiniteDuration.apply(45000 + new Random().nextInt(10), TimeUnit.MILLISECONDS),
				ActorMain, new StopRaft(), actorSystem.dispatcher(), ActorRef.noSender());
		
		actorSystem.scheduler().scheduleOnce(FiniteDuration.apply(65000 + new Random().nextInt(10), TimeUnit.MILLISECONDS),
				ActorMain, new StartRaft("Message"), actorSystem.dispatcher(), ActorRef.noSender());
		try {
			System.out.println(">>> Press ENTER to exit <<<");
			System.in.read();
		} catch (IOException ignored) {

		} finally {
			actorSystem.terminate();
		}
	}
}


