package ar.edu.utn.frc.backend.logistica.ms_transporte;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
@EnableFeignClients
public class MsTransporteApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsTransporteApplication.class, args);
	}

}
