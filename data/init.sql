CREATE TABLE submissions
(
    id       serial PRIMARY KEY,
    email    VARCHAR(50),
    toppings TEXT[]
);
CREATE UNIQUE INDEX submission_email ON submissions (email);

CREATE MATERIALIZED VIEW topping_vote_summary
AS
select topping, count(distinct id) as votes
from (SELECT id, email, toppings
      FROM public.submissions) as s,
     unnest(s.toppings) as topping
group by topping
WITH NO DATA;
CREATE UNIQUE INDEX topping_category ON topping_vote_summary (topping);

REFRESH
    MATERIALIZED VIEW topping_vote_summary;

CREATE OR REPLACE PROCEDURE refresh_topping_vote_summary_mat_view()
    LANGUAGE plpgsql
AS
$$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY topping_vote_summary;
END
$$;

-- CREATE TRIGGER refresh_topping_vote_summary_mat_view_after_submission
--     AFTER INSERT
--     ON submissions
--     FOR EACH STATEMENT
-- EXECUTE PROCEDURE refresh_topping_vote_summary_mat_view();