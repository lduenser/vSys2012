package lab3;

/**
 *
 * @author Ternek Marianne 0825379
 * Jänner 2012
 */

import java.util.ArrayList;

public class TaskList {
 //TaskList contains all tasks
   public static ArrayList<Task> taskList = new ArrayList<Task>(); // Liste nur für Task-Objekte

    /*****START NEED TO BE THREAD SAFE */
    //function and procedures for Tasks and the Tasklist:
    //update task in list
    public synchronized static void updateTask(Task task, UtilityClass.TaskStatus Status) {
            task.Status=Status;
    }

    public synchronized static void AddRequestedEngine(Task task, int engine_port, String engine_host) {
            task.AddRequestedEngine(engine_port,engine_host);
    }

    //Add a new Task to List
    public synchronized static void AddTask(Task task) {
            taskList.add(task);
    }
    
    //Get next TaskID
    public synchronized static int getNewTaskID() {
        int maxID=0;

        for (Task task : taskList) {
            if ((maxID==0) || (maxID<task.id)) {
                maxID=task.id;
            }
        }
        return maxID+1;
    }
    
    //Get specific task by id
    public synchronized static Task getTaskByID(int id) {
        Task returntask= null;

        for (Task task : taskList) {
            if (id==task.id) {
               returntask=task;
               break;
            }
        }
        return returntask;
    }
    /*****END NEED TO BE THREAD SAFE */
}
