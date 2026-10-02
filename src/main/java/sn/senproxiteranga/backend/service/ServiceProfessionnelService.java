package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ServiceRequest;
import sn.senproxiteranga.backend.dto.ServiceResponse;

import java.util.List;

public interface ServiceProfessionnelService {

    ServiceResponse creer(Long professionnelId, ServiceRequest request);

    ServiceResponse modifier(Long professionnelId, Long serviceId, ServiceRequest request);

    ServiceResponse activer(Long professionnelId, Long serviceId);

    ServiceResponse desactiver(Long professionnelId, Long serviceId);

    void supprimer(Long professionnelId, Long serviceId);

    List<ServiceResponse> listerParProfessionnel(Long professionnelId, boolean actifsSeulement);

    ServiceResponse trouverParId(Long serviceId);
}