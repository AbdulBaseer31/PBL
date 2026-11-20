package com.vrs.controller;

import com.vrs.model.Bill;
import com.vrs.security.CurrentUser;
import com.vrs.service.BillService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    public static class CreateBillRequest {
        public String reservationId;
    }

    @PostMapping
    public ResponseEntity<Bill.Detail> createBill(@RequestBody CreateBillRequest req,
                                                  HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        Bill.Detail billDetail = billService.createBill(req.reservationId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(billDetail);
    }

    @GetMapping
    public ResponseEntity<List<Bill.Detail>> getBills(HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        return ResponseEntity.ok(billService.getBills(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill.Detail> getBillById(@PathVariable String id,
                                                   HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute("currentUser");
        return ResponseEntity.ok(billService.getBillById(id, user));
    }
}

