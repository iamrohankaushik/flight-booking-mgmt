package com.example.demo.searching.controller

import com.example.demo.searching.dto.FlightScheduleDto
import com.example.demo.searching.dto.FlightSearchRequest
import com.example.demo.searching.service.SearchService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/search-flights")
class SearchController(
    private val searchService: SearchService
) {
    private val logger = LoggerFactory.getLogger(SearchController::class.java)

    @GetMapping
    fun search(
        @RequestParam source: String,
        @RequestParam dest: String,
        @RequestParam date: String
    ): ResponseEntity<List<FlightScheduleDto>> {
        logger.info("Received search request: source=$source, dest=$dest, date=$date")
        
        val request = FlightSearchRequest(source, dest, date)
        val results = searchService.searchFlights(request)
        
        logger.info("Returning ${results.size} schedules for request.")
        return ResponseEntity.ok(results)
    }
}
