package ar.edu.utn.frc.backend.logistica.ms_transporte.service;

import ar.edu.utn.frc.backend.logistica.ms_transporte.repository.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.ms_transporte.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.ms_transporte.dto.deposito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    public List<Deposito> findAll() {
        log.info("Obteniendo todos los depósitos");
        List<Deposito> lista = depositoRepository.findAll();
        log.debug("Cantidad de depósitos encontrados: {}", lista.size());
        return lista;
    }

    public List<Deposito> findAllActivos() {
        log.info("Obteniendo depósitos activos");
        List<Deposito> lista = depositoRepository.findByActivoTrue();
        log.debug("Depósitos activos encontrados: {}", lista.size());
        return lista;
    }

    public Deposito findById(int idDeposito) {
        log.info("Buscando depósito con ID {}", idDeposito);
        return depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));
    }

    public List<Deposito> findActivosByIdsKeepingOrder(List<Integer> ids) {
        log.info("Buscando depósitos activos en el orden solicitado: {}", ids);

        if (ids == null) {
            log.warn("Lista de IDs recibida es nula, devolviendo lista vacía");
            return List.of();
        }

        var all = depositoRepository.findAllById(ids);
        var byId = all.stream().collect(Collectors.toMap(Deposito::getIdDeposito, d -> d));

        List<Deposito> ordered = new ArrayList<>();

        for (Integer id : ids) {
            var d = byId.get(id);
            if (d == null) {
                log.warn("Depósito no encontrado: {}", id);
                throw new NoSuchElementException("Depósito no encontrado: " + id);
            }
            if (Boolean.FALSE.equals(d.getActivo())) {
                log.warn("Depósito inactivo: {}", id);
                throw new IllegalStateException("Depósito inactivo: " + id);
            }
            ordered.add(d);
        }

        log.debug("Depósitos obtenidos en orden: {}", ordered);
        return ordered;
    }

    public List<DepositoCercanoResponseDTO> buscarDepositosCercanos(Double lat, Double lng) {
        log.info("Buscando depósitos cercanos a lat={}, lng={}", lat, lng);

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
        log.info("Creando depósito con nombre '{}'", dto.getNombre());

        if (depositoRepository.existsByNombre(dto.getNombre())) {
            log.warn("Intento fallido: ya existe un depósito con el nombre '{}'", dto.getNombre());
            throw new IllegalStateException("Ya existe un depósito con ese nombre");
        }

        if (depositoRepository.existsByDireccion(dto.getDireccion())) {
            log.warn("Intento fallido: ya existe un depósito con la dirección '{}'", dto.getDireccion());
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

        log.info("Depósito creado correctamente con ID {}", guardado.getIdDeposito());

        return new DepositoCreateResponseDTO(
                guardado.getIdDeposito(),
                "Depósito creado correctamente"
        );
    }

    public DepositoResponseDTO actualizarDeposito(int id, DepositoUpdateRequestDTO dto) {
        log.info("Actualizando depósito con ID {}", id);

        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (!deposito.getActivo()) {
            log.warn("No se puede actualizar depósito {}: está inactivo", id);
            throw new IllegalStateException("No se puede actualizar un deposito inactivo");
        }

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        deposito.setCostoEstadiaDiario(dto.getCostoEstadiaDiario());

        depositoRepository.save(deposito);

        log.info("Depósito {} actualizado correctamente", id);

        return new DepositoResponseDTO(id, "Depósito actualizado correctamente");
    }

    public DepositoResponseDTO desactivar(int idDeposito) {
        log.info("Desactivando depósito con ID {}", idDeposito);

        Deposito deposito = depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (!deposito.getActivo()) {
            log.debug("Depósito {} ya estaba desactivado", idDeposito);
            return new DepositoResponseDTO(idDeposito, "El depósito ya estaba desactivado");
        }

        deposito.setActivo(false);
        depositoRepository.save(deposito);

        log.info("Depósito {} desactivado correctamente", idDeposito);

        return new DepositoResponseDTO(idDeposito, "Depósito desactivado correctamente");
    }

    public DepositoResponseDTO activar(int idDeposito) {
        log.info("Activando depósito con ID {}", idDeposito);

        Deposito deposito = depositoRepository.findById(idDeposito)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado"));

        if (deposito.getActivo()) {
            log.debug("Depósito {} ya estaba activado", idDeposito);
            return new DepositoResponseDTO(idDeposito, "El depósito ya estaba activado");
        }

        deposito.setActivo(true);
        depositoRepository.save(deposito);

        log.info("Depósito {} activado correctamente", idDeposito);

        return new DepositoResponseDTO(idDeposito, "Depósito activado correctamente");
    }

    private double distanciaEnKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                        Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
