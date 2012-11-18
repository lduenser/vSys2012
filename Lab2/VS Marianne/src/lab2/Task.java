package lab2;

/**
 *
 * @author ternekma
 */
 //Object for Tasks
public class Task {
 //Object for Tasks

     int id ;
     int expectedload;
     String filename;
     int engine_port;
     String engine_host ;
     String script;
     Company company;
     byte[] filecontent;
     UtilityClass.TaskStatus Status;
     String output;
     int costs;

     Task(int id, int expectedload,String filename, Company company,byte[] filecontent) {
      this.id=id;
      this.expectedload=expectedload;
      this.filename=filename;
      this.company=company;
      this.filecontent=filecontent;

      engine_port=0;
      engine_host="";
      script="";
      output="";
      costs=0;
      Status=UtilityClass.TaskStatus.statePrepared;
     }
}