package com.jordancalderwood.pizzatoppings

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.*
import org.springframework.scheduling.annotation.EnableScheduling;

@Component
class MaterializedViewRefresher(val db: JdbcTemplate) {
    @Transactional
    @Scheduled(fixedRate = 3000L)
    fun refresh() {
        db.update("call refresh_topping_vote_summary_mat_view();")
    }
}

@EnableScheduling
@SpringBootApplication
class PizzaToppingsApplication

fun main(args: Array<String>) {
    runApplication<PizzaToppingsApplication>(*args)
}

data class ToppingVote(val topping: String, val votes: Int)
data class ToppingsSubmission(val email: String, val toppings: Array<String>)

interface IToppingVoteService {
    fun listToppingVoteSummary(): List<ToppingVote>
    fun submitToppings(toppingsSubmission: ToppingsSubmission)
}

@Service
class ToppingVoteService(val db: JdbcTemplate) : IToppingVoteService {
    override fun listToppingVoteSummary(): List<ToppingVote> = db.query("SELECT * FROM topping_vote_summary") { response, _ ->
        ToppingVote(response.getString("topping"), response.getInt("votes"))
    }

    override fun submitToppings(toppingsSubmission: ToppingsSubmission) {
        db.update(
            "INSERT INTO submissions(email, toppings) VALUES ( ?, ? ) ON CONFLICT (email) DO UPDATE SET toppings = EXCLUDED.toppings",
            toppingsSubmission.email, toppingsSubmission.toppings
        )
    }
}

@RestController
class ToppingVoteController(val service: IToppingVoteService) {
    @GetMapping("/toppingVotes")
    fun listToppingVoteSummary() = service.listToppingVoteSummary()

    @PostMapping("/toppings")
    fun submitToppings(@RequestBody toppingsSubmission: ToppingsSubmission) {
        service.submitToppings(toppingsSubmission)
    }
}