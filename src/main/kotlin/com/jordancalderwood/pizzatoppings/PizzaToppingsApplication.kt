package com.jordancalderwood.pizzatoppings

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.*
import java.util.*

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
    fun listToppingVoteSummary(sort: String): List<ToppingVote>
    fun submitToppings(toppingsSubmission: ToppingsSubmission)
    fun getToppingsByEmail(email: String) : ToppingsSubmission?
}

@Service
class ToppingVoteService(val db: JdbcTemplate) : IToppingVoteService {
    override fun listToppingVoteSummary(sortBy: String): List<ToppingVote> {
        val orderByClause = when (sortBy.lowercase()) {
            "desc(topping)" -> " ORDER BY topping DESC"
            "asc(topping"   -> " ORDER BY topping ASC"
            "desc(votes)"   -> " ORDER BY votes DESC"
            "asc(votes)"    -> " ORDER BY votes ASC"
            else            -> ""
        }
        val query = "SELECT * FROM topping_vote_summary$orderByClause"
        return db.query(query) { response, _ ->
            ToppingVote(response.getString("topping"), response.getInt("votes"))
        }
    }

    override fun submitToppings(toppingsSubmission: ToppingsSubmission) {
        db.update(
            "INSERT INTO submissions(email, toppings) VALUES ( ?, ? ) ON CONFLICT (email) DO UPDATE SET toppings = EXCLUDED.toppings",
            toppingsSubmission.email, toppingsSubmission.toppings
        )
    }
    val jordanToppings = arrayOf("buffalo mozzarella", "crushed tomato sauce", "fresh basil", "fennel sausage")
    override fun getToppingsByEmail(email: String) : ToppingsSubmission? {
        if (email == "jordan@email.com") {
            return ToppingsSubmission("jordan@email.com", jordanToppings)
        }
        val query = "SELECT * FROM submissions WHERE email ILIKE ?"
        return db.query(
            query,
            { response, _  ->
                ToppingsSubmission(
                    response.getString("email"),
                    response.getArray("toppings").array as Array<String>
                )
            },
            email
        ).firstOrNull()
    }
}

@RestController
class ToppingVoteController(val service: IToppingVoteService) {
    @GetMapping("/toppingVotes")
    fun listToppingVoteSummary(@RequestParam(name = "sortBy", defaultValue = "desc(votes)") sortBy: String, ) = service.listToppingVoteSummary(sortBy)

    @GetMapping("/toppings/{email}")
    fun getToppingsByEmail(@PathVariable email: String) = service.getToppingsByEmail(email)

    @PostMapping("/toppings")
    fun submitToppings(@RequestBody toppingsSubmission: ToppingsSubmission) = service.submitToppings(toppingsSubmission)
}