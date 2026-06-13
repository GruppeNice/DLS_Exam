create table if not exists rating (
                                      id TEXT not null primary key,
                                      user_id TEXT not null,
                                      movie_id TEXT not null,
                                      user_rating INTEGER not null,
                                      created_at TEXT,
                                      updated_at TEXT,
                                      unique(user_id, movie_id)
);

create table if not exists review (
                                      id TEXT not null primary key,
                                      user_id TEXT not null,
                                      movie_id TEXT not null,
                                      review_text TEXT,
                                      spoiler INTEGER,
                                      created_at TEXT,
                                      updated_at TEXT,
                                      unique(user_id, movie_id)
);

create table if not exists review_vote (
                                           id TEXT not null primary key,
                                           review_id TEXT not null,
                                           user_id TEXT not null,
                                           value INTEGER not null,
                                           foreign key (review_id) references review(id),
                                           unique(review_id, user_id)
);