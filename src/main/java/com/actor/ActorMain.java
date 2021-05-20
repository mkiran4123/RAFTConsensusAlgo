package com.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.actions.RestartRaft;
import com.actions.StartRaft;
import com.actions.StopRaft;
import com.actions.Trigger;
import com.election.ResponseVote;
import com.election.RequestForVotes;
import com.logReplication.Log;
import com.logReplication.LogResponse;
import com.logReplication.NewLogRequest;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
 
/**
* actor that changes behavior
*/
public class ActorMain {
	
   static ArrayList<ActorRef> actorList = new ArrayList<ActorRef>();
   static ActorRef electedLeader = null;
   
   static class ActorChild extends AbstractActor {
	   private Receive follower;
       private Receive candidate;
       private Receive leader;
	   private int commitLength;
       private Map<ActorRef, Integer> responseLength;
       private Map<ActorRef, Integer> requestLength;
       private HashSet<Integer> votesReceived;
       private ArrayList<Log> log;
       private int presentTerm;
       private ActorRef castedVote;
       private ActorRef presentLeader;
      

       
       public ActorRef getpresentLeader() {
           return presentLeader;
       }
       public void setCurrentLeader(ActorRef presentLeader) {
    	   this.presentLeader = presentLeader;
       }
       public int getCommitLength() {
		  return commitLength;
       }
       public void setCommitLength(int commitLength) {
    	  this.commitLength = commitLength;
    			  }
       public Map<ActorRef, Integer> getResponseLengthh() {
		  return responseLength;
		}
       public void setResponseLength(Map<ActorRef, Integer> responseLength) {
		  this.responseLength = responseLength;
		}
       public Map<ActorRef, Integer> getRequestLength() {
		  return requestLength;
		}
       public void setRequestLength(Map<ActorRef, Integer> requestLength) {
		  this.requestLength = requestLength;
		}
       public ArrayList<Log> getLog() {
		  return log;
		}
       public void setLog(ArrayList<Log> log) {
		  this.log = log;
		}
       public int getPresentTerm() {
		  return presentTerm;
		}
       public void setpresentTerm(int currentTerm) {
		  this.presentTerm = currentTerm;
		}
       public ActorRef getcastedVote() {
		  return castedVote;
		}
       public void setcastedVote(ActorRef castedVote) {
		  this.castedVote = castedVote;
		}
       public HashSet<Integer> getVotesReceived() {
		  return votesReceived;
		}
       public void setVotesReceived(HashSet<Integer> votesReceived) {
		  this.votesReceived = votesReceived;
		}
       
       public static Props props() {
    	   return Props.create(ActorChild.class, 0, new HashMap<Integer, Integer>(), new HashMap<Integer, Integer>(),
    			   new ArrayList<Log>(), 0, null, new HashSet<Integer>(), null);
       
       }
       
       public ActorChild(int commitLength, Map<ActorRef, Integer> responseLength, Map<ActorRef, Integer> requestLength, 
    		   ArrayList<Log> log, int presentTerm, ActorRef castedVote, HashSet<Integer> votesReceived,
    		   ActorRef presentLeader) {
    	   super();
           this.commitLength = commitLength;
           this.responseLength = responseLength;
           this.requestLength = requestLength;
           this.log = log;
           this.presentTerm = presentTerm;
           this.castedVote = castedVote;
           this.votesReceived = votesReceived;
           this.presentLeader = presentLeader;
       }
       
       public Receive createReceive() {
       		follower = receiveBuilder()
       				.match(ReceiveTimeout.class, this::timeOutTrigger)
                    .match(RequestForVotes.class, this::onRequestForVote)
                    .match(ResponseVote.class, this::onResponseVote)
                    .match(StartRaft.class, this::onReciveMessage)
                    .match(StopRaft.class, this::stopAnActor)
                    .match(RestartRaft.class, this::onRestart)
                    .match(NewLogRequest.class, this::onNewLogRequest).build();
       		
       		
       		leader = receiveBuilder()
       				.match(StartRaft.class, this::onReciveMessage)
                    .match(RestartRaft.class, this::onRestart)
                    .match(StopRaft.class, this::stopAnActor)
                    .match(LogResponse.class, this::onLogResponse)
                    .build();
       		
       		candidate = receiveBuilder()
       				.match(RequestForVotes.class, this::onRequestForVote)
                    .match(ResponseVote.class, this::onResponseVote)
                    .match(StartRaft.class, this::onReciveMessage)
                    .match(NewLogRequest.class, this::onNewLogRequest)
                    .match(RestartRaft.class, this::onRestart)
                    .match(StopRaft.class, this::stopAnActor)
                    .build();
       		

       		getContext().setReceiveTimeout(Duration.apply(5 + new Random().nextInt(10), TimeUnit.SECONDS));
       		return follower;
       	}

       private void onRestart(RestartRaft r) {
    	   System.out.println("Restarting the RAFT System");
    	   throw new RuntimeException();
       }
       
       private void stopAnActor(StopRaft stop) {
    	   System.out.println("Stopping the RAFT System");
    	   self().tell(akka.actor.PoisonPill.getInstance(), ActorRef.noSender());
    	   actorList.remove(self());
       }
       
       private void timeOutTrigger(ReceiveTimeout c) {
    	   System.out.println("Timeout has occured");
    	   int previousTerm = 0;
    	   getContext().become(candidate);
    	   castedVote = self();
    	   presentTerm++;
    	   votesReceived.add(self().hashCode());
    	   if (log.size() > 0) {
    		   previousTerm = log.get(log.size() - 1).getTerm();
    	   }
    	   for (ActorRef actorRef : actorList) {
    		   if (actorRef != self()) {
    			   actorRef.tell(new RequestForVotes(presentTerm, log.size(), previousTerm), self());
    			}
    		}
    	   getContext().setReceiveTimeout(Duration.apply(10 + new Random().nextInt(10), TimeUnit.SECONDS));
    	   
       }
       
       
       private void onRequestForVote(RequestForVotes voteRequest) {
    	   int newTerm = 0;
    	   boolean logCheck = (voteRequest.getLastTerm() > newTerm) || (voteRequest.getLastTerm() == newTerm && voteRequest.getLogLength() >= log.size());
    	   boolean termCheck = voteRequest.getCurrentTerm() > presentTerm || ((voteRequest.getCurrentTerm() == presentTerm) && (castedVote == null || castedVote == sender()));
    	   if (!log.isEmpty()) {
    		   newTerm = log.get(log.size() - 1).getTerm();
    	   }
    	   if (logCheck && termCheck) {
    		   presentTerm = voteRequest.getCurrentTerm();
    		   getContext().become(follower);
    		   castedVote = sender();
    		   System.out.println("Actor "+ self().hashCode() + " has voted for Actor " + sender().hashCode());
	           sender().tell(new ResponseVote(presentTerm, true), getContext().self());
	           getContext().setReceiveTimeout(Duration.Undefined());
    	   } 
    	   else {
    		   sender().tell(new ResponseVote(presentTerm, false), getContext().self());
    	   }
       }
       
       private void onResponseVote(ResponseVote voteResponse) {
    	   if (voteResponse.getTerm() == presentTerm && voteResponse.isVoated()) {
    		   votesReceived.add(sender().hashCode());
    		   if(actorList.size() % 2 == 0) {
    			   if (votesReceived.size() > (actorList.size() + 1) / 2) {
    				   getContext().become(leader);
    		    	   presentLeader = self();
    		    	   electedLeader = self();
    		    	   System.out.println("Actor "+presentLeader.hashCode()+" elected as a leader");
    		    	   for (ActorRef actor : actorList) {
    		    		   if (actor != self()) {
    		    			   responseLength.put(actor, log.size());
    		    			   responseLength.put(actor, 0);
    		    			   logReplicating(actor);
    		    		   }
    		    	   }
    				   getContext().system().scheduler().schedule(FiniteDuration.apply(0, TimeUnit.MILLISECONDS), FiniteDuration.apply(5000, TimeUnit.MILLISECONDS), self(), new com.election.check(), getContext().system().dispatcher(), ActorRef.noSender());
    			   }
    		   }
    		   else {
    			   if (votesReceived.size() == (actorList.size() + 1) / 2) {
    				   getContext().become(leader);
    		    	   presentLeader = self();
    		    	   electedLeader = self();
    		    	   System.out.println("Actor "+presentLeader.hashCode()+" elected as a leader");
    		    	   for (ActorRef actor : actorList) {
    		    		   if (actor != self()) {
    		    			   responseLength.put(actor, log.size());
    		    			   responseLength.put(actor, 0);
    		    			   logReplicating(actor);
    		    		   }
    		    	   }
    				   getContext().system().scheduler().schedule(FiniteDuration.apply(0, TimeUnit.MILLISECONDS), FiniteDuration.apply(5000, TimeUnit.MILLISECONDS), self(), new com.election.check(), getContext().system().dispatcher(), ActorRef.noSender());
    			   }
    		   	}
    		  } 
    	   else if (voteResponse.getTerm() > presentTerm) 
    	   {
    		   SplitVotes(voteResponse);
    	   }
       }
       
       private void SplitVotes(ResponseVote voteResponse) {
    	   presentTerm = voteResponse.getTerm();
           getContext().become(follower);
           castedVote = null;
       }
       
       private void onReciveMessage(StartRaft clientMessage) {
		   System.out.println("----Received a Message----"); 
		   if (electedLeader != null && self() == electedLeader)
		   {
			   log.add(new Log(presentTerm, clientMessage.getMessage()));
			   requestLength.put(self(), log.size());
			   for (ActorRef actorRef : actorList) {
				if (actorRef != presentLeader) {
						logReplicating(actorRef);
					}
				}
		   } else  if(electedLeader == null){
			   electedLeader.tell(clientMessage, ActorRef.noSender());
		   } else {
				System.out.println("------Message Forwarded To Leader-------");
				electedLeader.forward(clientMessage, getContext());
		   }
       }
       
       private void logReplicating(ActorRef actor) {
    	   ArrayList<Log> checkLogEntries = new ArrayList<Log>();
    	   int resLength = responseLength.get(actor);
    	   int previousTerm = 0;
    	   if (resLength > 0) {
    		   previousTerm = log.get(responseLength.get(actor) - 1).getTerm();
    	   }
    	   for (int logLen = resLength; logLen < log.size(); logLen++) {
    		   checkLogEntries.add(log.get(logLen));
    	   }
    	   System.out.println("------New Log Request from Leader-------");
    	   System.out.println(new NewLogRequest(presentTerm, resLength, previousTerm, checkLogEntries, commitLength));
    	   getContext().system().scheduler().scheduleOnce(FiniteDuration.apply(0, TimeUnit.MILLISECONDS), actor,
    			   new NewLogRequest(presentTerm, resLength, previousTerm, checkLogEntries, commitLength), getContext().system().dispatcher(),self());
       }
       
       private void onNewLogRequest(NewLogRequest logRequest) {
    	   boolean checkedLog = log.size() >= logRequest.getresponseLength();
    	   if (logRequest.getTerm() > presentTerm) {
    		   presentTerm = logRequest.getTerm();
    		   castedVote = null;
    	   }
    	   if (checkedLog && logRequest.getresponseLength() > 0) {
    		   checkedLog = logRequest.getPreviousTerm() == log.get(logRequest.getresponseLength() - 1).getTerm();
    	   }
	       if (logRequest.getTerm() == presentTerm && checkedLog) {
	    	   getContext().become(follower);
	    	   presentLeader = sender();
	    	   addEntriesToLog(logRequest.getresponseLength(), logRequest.getCommitlength(), logRequest.getLog());
	    	   System.out.println("follower " + self().hashCode() + " log entires" + log.toString());
	    	   electedLeader.tell(new LogResponse(logRequest.getTerm(), logRequest.getresponseLength() + logRequest.getLog().size(), true), self());
	        } 
	       else {
	    	   electedLeader.tell(new LogResponse(logRequest.getTerm(), 0, false), self());
	       }
	       getContext().setReceiveTimeout(Duration.apply(10 + new Random().nextInt(10), TimeUnit.SECONDS));
       }
       
       private void addEntriesToLog(int sentLength2, int commitLength2, ArrayList<Log> entries) {
    	   if (entries.size() > 0 && log.size() > sentLength2) {
    		   if (log.get(sentLength2).getTerm() != entries.get(0).getTerm()) {
    			   for (int i = sentLength2; i < log.size(); i++) {
    				   log.remove(i);
					}
    			   }
    		   }
    	   if (entries.size() + sentLength2 > log.size()) {
    		   for (int i = log.size() - sentLength2; i < entries.size(); i++) {
    			   log.add(entries.get(i));
    			  }
    		   }
    	   if (commitLength2 > commitLength) {
    		   commitLength = commitLength2;
    	   }
       }
       
       private void onLogResponse(LogResponse logResponse) {
    	   int logSize = (actorList.size() + 1) / 2;
    	   if (logResponse.getTerm() == presentTerm) {
    		   if (logResponse.isSuccess()) {
    			   responseLength.put(sender(), logResponse.getAcknowledgement());
    			   requestLength.put(sender(), logResponse.getAcknowledgement());
    	    	   ArrayList<Integer> list = new ArrayList<Integer>();
    	    	   for (int val = 0; val < log.size(); val++) {
    	    		   if (received(val) >= logSize) {
    	    			   list.add(val);
    	    			  }
    	    		   }
    	    	   if (!list.isEmpty() && Collections.max(list) > commitLength && log.get(Collections.max(list) - 1).getTerm() == presentTerm) {
    	    		   commitLength = Collections.max(list);
    	    	   }
    			} 
    		   else if (responseLength.get(sender()) > 0) {
    			   responseLength.put(sender(), responseLength.get(sender()) - 1);
    			   }
    		   } else if (logResponse.getTerm() > presentTerm) {
    			   presentTerm = logResponse.getTerm();
    			   getContext().become(follower);
    			   castedVote = null;
	           } 
    	   }
       

       private int received(int val) {
    	   int count = 0;
    	   for (int j = 0; j < actorList.size(); j++) {
    		   try {
    			   if (requestLength.get(actorList.get(j)) >= val) {
    				   count++;
    				 }
    		   } catch (NullPointerException e) {

    		   }
    	   }
    	   return count;
       }
   }
   
   public static class MainActor extends AbstractActor {

	   
		@Override
		public Receive createReceive() {
			return receiveBuilder()
					.match(Trigger.class, this::onInit)
					.match(StartRaft.class, this::onStart)
					.match(StopRaft.class, this::onStop)
					.match(RestartRaft.class, this::onRestart)
					.build();

			
		}
		
	   private void onInit(Trigger trigger) {
		   for (int i = 0; i < 5; i++) {
			   final ActorRef actor = getContext().actorOf(ActorChild.props());
			   System.out.println("Actors "+i+" "+actor.hashCode());
			   actorList.add(actor);
		   }
	   }
		
	   private void onStart(StartRaft message) {
		   for (int i = 0; i < 5; i++) {
			   getContext().system().scheduler().scheduleOnce(FiniteDuration.apply(20000 + 1000 * new Random().nextInt(5), TimeUnit.MILLISECONDS),
					   actorList.get(new Random().nextInt(actorList.size())), message, getContext().system().dispatcher(),
					   self());
           	}
	   }
		
	   private void onRestart(RestartRaft restart) {
		   actorList.get(new Random().nextInt(actorList.size())).tell(restart, self());
       }
	   
	   private void onStop(StopRaft stop) {
		   actorList.get(new Random().nextInt(actorList.size())).tell(stop, self());
	   }
	   
	   public SupervisorStrategy supervisorStrategy() {
		   return new OneForOneStrategy(10, Duration.apply(1, TimeUnit.SECONDS),
				   DeciderBuilder.match(RuntimeException.class, e -> SupervisorStrategy.restart())
				   .match(NullPointerException.class, e -> SupervisorStrategy.resume()).build());
	   }
	   
   	}
  }
