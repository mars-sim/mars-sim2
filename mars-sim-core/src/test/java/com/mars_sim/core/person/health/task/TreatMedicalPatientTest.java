package com.mars_sim.core.person.health.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.structure.building.function.FunctionType;

public class TreatMedicalPatientTest extends AbstractMarsSimUnitTest {

    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        sb.getMedical().requestTreatment(hp);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = TreatMedicalPatient.createTask(doctor);
        assertFalse("Task created", task.isDone());
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(doctor, task, 1000);
        executeTask(doctor, task, 1);
        assertTrue("Health problem treated at Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));
        assertFalse("Health problem not waiting at Medical care", sb.getMedical().getProblemsAwaitingTreatment().contains(hp));

        // Complete treatment
        executeTaskForDuration(doctor, task, recoveryTime * 1.5);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, patient.getPhysicalCondition().getProblems().size());

        assertTrue("Complaint in recovery", hp.getRecovering());
        assertFalse("Health problem removed from Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));

    }


    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new TreatMedicalPatientMeta();

        // Self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.LACERATION);
        sb.getMedical().requestTreatment(hp);

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);
        var tasks = mt.getTaskJobs(doctor);
        assertTrue("No dotor health problems", tasks.isEmpty());

        // Not self healing
        hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        sb.getMedical().requestTreatment(hp);
        tasks = mt.getTaskJobs(doctor);
        assertFalse("Problems found", tasks.isEmpty());
    }
}
