package lab2;

import java.util.ArrayList;

/**
 *
 * @author ternekma
 */
public class TaskList {
 //TaskList contains all tasks
   public static ArrayList<Task> taskList = new ArrayList<Task>(); // Liste nur für Task-Objekte

        /*****START NEED TO BE THREAD SAFE */
    //function and procedures for Tasks and the Tasklist:
    //update task in list
    public synchronized static void updateTask(Task task, String sengine_host, int engine_port, UtilityClass.TaskStatus Status) {
            task.engine_host=sengine_host;
            task.engine_port=engine_port;
            task.Status=Status;
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
