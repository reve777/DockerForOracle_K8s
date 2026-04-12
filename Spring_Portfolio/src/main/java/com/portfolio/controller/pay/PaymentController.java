package com.portfolio.controller.pay;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ecpay")
public class PaymentController {

    private final EcpayService ecpayService;

    public PaymentController(EcpayService ecpayService) {
        this.ecpayService = ecpayService;
    }
    //http://localhost:8084/portfolio/ecpay/pay
    @GetMapping("/pay")
    public String pay() {
        return ecpayService.createOrder();
    }

    @PostMapping("/callback")
    public String callback() {
        return "1|OK";
    }
}