package com.provision.backend.readingimports;

import com.provision.backend.meter.ElectricityMeter;
import com.provision.backend.meter.ElectricityMeterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebugReadingImportCsvGeneratorTest {
    @Mock
    ElectricityMeterRepository meterRepository;

    @InjectMocks
    DebugReadingImportCsvGenerator generator;

    @Test
    void generatesImportCompatibleCsv() throws Exception {
        ElectricityMeter meter = mock(ElectricityMeter.class);
        when(meter.getSerialNumber()).thenReturn("SN-1");
        when(meterRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(meter));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        StreamingResponseBody body = generator.generate(2);
        body.writeTo(output);

        String csv = output.toString(StandardCharsets.UTF_8);
        assertThat(csv).startsWith("meter_serial_number,measured_at,zone_t1,zone_t2,zone_t3");
        assertThat(csv.lines()).hasSize(3);
        assertThat(csv).contains("SN-1");
    }

    @Test
    void rejectsInvalidRowCount() {
        assertThatThrownBy(() -> generator.generate(0))
                .isInstanceOf(ReadingImportException.class)
                .hasMessageContaining("от 1 до 1000000");
    }
}
