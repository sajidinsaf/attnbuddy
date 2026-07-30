package com.visibleai.brasstacks.domain;

import com.visibleai.brasstacks.domain.dto.DomainRequest;
import com.visibleai.brasstacks.domain.dto.DomainResponse;
import com.visibleai.brasstacks.model.LifeDomain;
import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.LifeDomainRepository;
import com.visibleai.brasstacks.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DomainService {

    private final LifeDomainRepository domainRepository;
    private final UserRepository userRepository;

    public DomainService(LifeDomainRepository domainRepository, UserRepository userRepository) {
        this.domainRepository = domainRepository;
        this.userRepository = userRepository;
    }

    public List<DomainResponse> listDomains(Long userId) {
        return domainRepository.findByUserIdOrderByPositionAsc(userId).stream()
                .map(DomainResponse::from)
                .toList();
    }

    @Transactional
    public DomainResponse createDomain(Long userId, DomainRequest request) {
        User user = userRepository.getReferenceById(userId);
        List<LifeDomain> existing = domainRepository.findByUserIdOrderByPositionAsc(userId);
        int nextPosition = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getPosition() + 1;

        LifeDomain domain = new LifeDomain(user, request.name(), request.color(), request.weight(), nextPosition);
        domain.setActiveStart(request.activeStart());
        domain.setActiveEnd(request.activeEnd());
        return DomainResponse.from(domainRepository.save(domain));
    }

    @Transactional
    public DomainResponse updateDomain(Long userId, Long domainId, DomainRequest request) {
        LifeDomain domain = domainRepository.findById(domainId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Domain not found"));

        domain.setName(request.name());
        domain.setColor(request.color());
        domain.setWeight(request.weight());
        domain.setActiveStart(request.activeStart());
        domain.setActiveEnd(request.activeEnd());
        return DomainResponse.from(domainRepository.save(domain));
    }

    @Transactional
    public void deleteDomain(Long userId, Long domainId) {
        LifeDomain domain = domainRepository.findById(domainId)
                .filter(d -> d.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Domain not found"));
        domainRepository.delete(domain);
    }
}
