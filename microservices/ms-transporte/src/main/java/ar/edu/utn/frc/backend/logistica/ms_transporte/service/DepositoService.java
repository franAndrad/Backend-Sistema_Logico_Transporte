package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.DepositoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    public List<Deposito> findAll() {
        return depositoRepository.findAll();
    }

    public Deposito findById(Integer idDeposito) {
        return depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));
    }

    public Deposito save(Deposito deposito) {
        if (depositoRepository.existsByNombre(deposito.getNombre())) {
            throw new RuntimeException("Ya existe un depósito con ese nombre");
        }
        return depositoRepository.save(deposito);
    }
}
