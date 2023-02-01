CREATE TABLE submissions
(
    id        serial PRIMARY KEY,
    email     VARCHAR(50),
    toppings  TEXT []
);

CREATE MATERIALIZED VIEW topping_vote_summary
AS
select topping, count(distinct id) as votes
from (SELECT DISTINCT
    ON (email) id, email, toppings
      FROM public.submissions
      ORDER BY email, id desc) as latest_submissions,
     unnest(latest_submissions.toppings) as topping
group by topping WITH NO DATA;
CREATE UNIQUE INDEX topping_category ON topping_vote_summary (topping);

REFRESH
    MATERIALIZED VIEW topping_vote_summary;

CREATE OR REPLACE FUNCTION refresh_topping_vote_summary_mat_view()
    RETURNS TRIGGER LANGUAGE plpgsql
AS $$
BEGIN
    REFRESH
        MATERIALIZED VIEW CONCURRENTLY topping_vote_summary;
    RETURN NULL;
END $$;

CREATE TRIGGER refresh_topping_vote_summary_mat_view_after_submission
    AFTER INSERT
    ON submissions
    FOR EACH STATEMENT
EXECUTE PROCEDURE refresh_topping_vote_summary_mat_view();