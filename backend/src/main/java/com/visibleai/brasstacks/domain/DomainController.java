package com.visibleai.brasstacks.domain;

import com.visibleai.brasstacks.domain.dto.DomainRequest;
import com.visibleai.brasstacks.domain.dto.DomainResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
public class DomainController {

    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping
    public List<DomainResponse> list(Authentication auth) {
        return domainService.listDomains(getUserId(auth));
    }

    @PostMapping
    public ResponseEntity<DomainResponse> create(Authentication auth, @Valid @RequestBody DomainRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(domainService.createDomain(getUserId(auth), request));
    }

    @PutMapping("/{id}")
    public DomainResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody DomainRequest request) {
        return domainService.updateDomain(getUserId(auth), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        domainService.deleteDomain(getUserId(auth), id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }
}
