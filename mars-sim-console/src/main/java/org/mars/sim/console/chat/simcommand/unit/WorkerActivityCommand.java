package org.mars.sim.console.chat.simcommand.unit;

import java.util.List;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.CommandHelper;
import org.mars.sim.console.chat.simcommand.StructuredResponse;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager.OneActivity;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/** 
 * 
 */
public class WorkerActivityCommand extends AbstractUnitCommand {
	
	public WorkerActivityCommand(String group) {
		super(group, "ac", "activities", "Activites done by the Worker");
	}

	/** 
	 * Output the current immediate location of the Unit
	 */
	@Override
	protected boolean execute(Conversation context, String input, Unit target) {

		TaskManager tManager = null;

		if (target instanceof Worker) {
			tManager = ((Worker)target).getTaskManager();
		}
		else {
			context.println("Sorry I am not a Worker.");
			return false;
		}

		StructuredResponse response = new StructuredResponse();

		response.appendTableHeading("When", 4,
									"Task", -CommandHelper.TASK_WIDTH,
									"Phase");

		// TODO allow optional inout to choose a day
		List<OneActivity> activities = tManager.getTodayActivities();

		for (OneActivity attr : activities) {
			response.appendTableRow(String.format("%3d", attr.getStartTime()),
									attr.getTaskName(),
									attr.getPhase());
		}
		context.println(response.getOutput());
		
		return true;
	}
}
