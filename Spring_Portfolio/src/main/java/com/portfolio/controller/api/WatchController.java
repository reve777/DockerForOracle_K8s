package com.portfolio.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.portfolio.entity.Watch;
import com.portfolio.service.WatchService;

@RestController
@RequestMapping("/watch")
public class WatchController {

    private final WatchService watchService;

    public WatchController(WatchService watchService) {
        this.watchService = watchService;
    }

    @GetMapping("/{id}")
    public Watch get(@PathVariable Integer id) {
        return watchService.getById(id);
    }

    @GetMapping
    public List<Watch> query() {
        return watchService.getAll();
    }

    @PostMapping("/{id}/add/{tstockId}")
    public Watch addTStock(@PathVariable Integer id, @PathVariable Integer tstockId) {
        return watchService.addTStock(id, tstockId);
    }

    @DeleteMapping("/{id}/remove/{tstockId}")
    public Watch removeTStock(@PathVariable Integer id, @PathVariable Integer tstockId) {
        return watchService.removeTStock(id, tstockId);
    }

    @PutMapping("/{id}")
    public Watch update(@PathVariable Integer id, @RequestBody Map<String, String> map) {
        return watchService.update(id, map);
    }
}
