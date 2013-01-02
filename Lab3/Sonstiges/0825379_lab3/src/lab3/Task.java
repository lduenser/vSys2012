package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.util.ArrayList;

 //Object for Tasks
public class Task {
 //Object for Tasks

     int id ;
     int expectedload;
     String filename;
     String script;
     Company company;
     byte[] filecontent;
     UtilityClass.TaskStatus Status;
     int costs;
     int finishedTaskcount=0;

     public class RequestedEngine {
       int port ;
       String host;

       RequestedEngine (int port, String host) {
           this.port=port;
           this.host=host;
       }
     }

     public class Output implements Comparable<Output> {
       int id ;
       String outputString;

        Output (int id, String outputString) {
           this.id=id;
           this.outputString=outputString;
       }

       public int compareTo(Output argument) {
            if( id < argument.id )
                return -1;
            if( id > argument.id )
                return 1;

            return 0;
      }
     }
    
     private ArrayList<RequestedEngine> requestedEngineList = new ArrayList<RequestedEngine>();
     private ArrayList<Output> outputList = new ArrayList<Output>();

     Task(int id, int expectedload,String filename, Company company,byte[] filecontent) {
      this.id=id;
      this.expectedload=expectedload;
      this.filename=filename;
      this.company=company;
      this.filecontent=filecontent;
      script="";
      costs=0;
      Status=UtilityClass.TaskStatus.statePrepared;
     }

     public String getEngineHosts() {
         String engines="";
         for (RequestedEngine engine : requestedEngineList) {
             if (engines.isEmpty()) {
                 engines=engine.host;
             } else {
                 engines=engines+", "+engine.host;
             }
         }

         return engines;
     }

     public String getOutput() {
         String outputString="";
         for (Output output : outputList) {
             if (outputString.isEmpty()) {
                 outputString=output.outputString;
             } else {
                 outputString=outputString+output.outputString;
             }
         }
         return outputString;
     }

     public void AddRequestedEngine(int engine_port, String engine_host) {
          requestedEngineList.add(new RequestedEngine(engine_port,engine_host));
     }

     public synchronized void AddOutput(int id, String Output) {
          Output myoutput=null;
          for (Output output : outputList) {
              if (output.id==id) {
                  myoutput=output;
                  break;
              }
          }
          if (myoutput==null) {
             myoutput=new Output(id,Output + "\n");
             outputList.add(myoutput);
             java.util.Collections.sort( outputList );
          } else {
               myoutput.outputString = myoutput.outputString+ Output + "\n";
          }     
     }

     public synchronized void AddFinishedTaskCount() {
        finishedTaskcount++;
     }

     public synchronized int GetFinishedTaskCount() {
        return finishedTaskcount;
     }
}