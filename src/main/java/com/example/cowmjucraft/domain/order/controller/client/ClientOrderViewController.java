package com.example.cowmjucraft.domain.order.controller.client;

import com.example.cowmjucraft.domain.order.service.OrderQueryByTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class ClientOrderViewController {

    private final OrderQueryByTokenService orderQueryByTokenService;

    @GetMapping(value = "/view", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> viewOrderByToken(@RequestParam("token") String token) {
        String html = orderQueryByTokenService.buildOrderViewHtml(token);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .body(html);
    }
}
