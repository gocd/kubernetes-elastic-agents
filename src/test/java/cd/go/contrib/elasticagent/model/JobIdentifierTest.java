package cd.go.contrib.elasticagent.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JobIdentifierTest {
    @Test
    public void shouldMatchJobIdentifier() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        final JobIdentifier deserializedJobIdentifier = JobIdentifier.fromJson(jobIdentifier.toJson());

        assertTrue(jobIdentifier.equals(deserializedJobIdentifier));
    }

    @Test
    public void shouldCreateRepresentationFromJobIdentifier() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getRepresentation(), is("up42/98765/stage_1/30000/job_1"));
    }

    @Test
    public void shouldCreatePipelineHistoryPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getPipelineHistoryPageLink(), is("/go/tab/pipeline/history/up42"));
    }

    @Test
    public void shouldCreateVSMPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getVsmPageLink(), is("/go/pipelines/value_stream_map/up42/98765"));
    }

    @Test
    public void shouldCreateStageDetailsPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getStageDetailsPageLink(), is("/go/pipelines/up42/98765/stage_1/30000"));
    }

    @Test
    public void shouldCreateJobDetailsPageLink() {
        final JobIdentifier jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);

        assertThat(jobIdentifier.getJobDetailsPageLink(), is("/go/tab/build/detail/up42/98765/stage_1/30000/job_1"));
    }

}