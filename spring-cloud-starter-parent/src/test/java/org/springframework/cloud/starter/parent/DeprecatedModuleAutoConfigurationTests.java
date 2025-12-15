package org.springframework.cloud.starter.parent;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
public class DeprecatedModuleAutoConfigurationTests {

	@Test
	public void logsWarning(CapturedOutput output) {
		Assertions.assertThat(output).contains(DeprecatedModuleAutoConfiguration.MESSAGE);
	}

	@SpringBootApplication
	static class TestConfiguration {

	}
}
