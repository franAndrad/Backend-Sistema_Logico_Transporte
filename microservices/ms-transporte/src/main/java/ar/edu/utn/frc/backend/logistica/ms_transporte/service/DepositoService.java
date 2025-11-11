package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.*;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    public List<Deposito> findAll() {
        return depositoRepository.findAll();
    }

    public List<Deposito> findAllActivos() {
        return depositoRepository.findByActivoTrue();
    }

    public Deposito findById(int idDeposito) {
        return depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));
    }

    public List<DepositoCercanoResponseDTO> buscarDepositosCercanos(Double lat, Double lng) {
        List<Deposito> depositos = depositoRepository.findByActivoTrue();

        return depositos.stream()
                .map(d -> new DepositoCercanoResponseDTO(
                        d.getIdDeposito(),
                        d.getNombre(),
                        BigDecimal.valueOf(
                                distanciaEnKm(lat, lng, d.getLatitud().doubleValue(), d.getLongitud().doubleValue())
                        ).setScale(2, RoundingMode.HALF_UP).doubleValue()
                ))
                .sorted((a, b) -> Double.compare(a.getDistanciaKm(), b.getDistanciaKm()))
                .toList();
    }

    public DepositoCreateResponseDTO save(DepositoCreateRequestDTO dto) {
        if (depositoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalStateException("Ya existe un depósito con ese nombre");
        }

        if (depositoRepository.existsByDireccion(dto.getDireccion())) {
            throw new IllegalStateException("Ya existe un depósito con esa dirección");
        }

        Deposito deposito = new Deposito();
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        deposito.setCostoEstadiaDiario(dto.getCostoEstadiaDiario());
        deposito.setActivo(true);

        Deposito guardado = depositoRepository.save(deposito);
        return new DepositoCreateResponseDTO(
                guardado.getIdDeposito(),
                "Depósito creado correctamente"
        );
    }

    public DepositoResponseDTO actualizarDeposito(int id, DepositoUpdateResponseDTO dto) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (!deposito.getActivo()) {
            throw new IllegalStateException("No se puede actualizar un deposito inactivo");
        }

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        deposito.setCostoEstadiaDiario(dto.getCostoEstadiaDiario());

        depositoRepository.save(deposito);

        return new DepositoResponseDTO(id, "Depósito actualizado correctamente");
    }

    public DepositoResponseDTO desactivar(int idDeposito) {
        Deposito deposito = depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (!deposito.getActivo()) {
            return new DepositoResponseDTO(idDeposito, "El depósito ya estaba desactivado");
        }

        deposito.setActivo(false);
        depositoRepository.save(deposito);

        return new DepositoResponseDTO(idDeposito, "Depósito desactivado correctamente");
    }

    public DepositoResponseDTO activar(int idDeposito) {
        Deposito deposito = depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (deposito.getActivo()) {
            return new DepositoResponseDTO(idDeposito, "El depósito ya estaba activado");
        }

        deposito.setActivo(true);
        depositoRepository.save(deposito);

        return new DepositoResponseDTO(idDeposito, "Depósito activado correctamente");
    }

    private double distanciaEnKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}