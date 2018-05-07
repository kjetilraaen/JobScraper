library(dplyr)
library(tidytext)
library(tidyr)
library(lubridate)
library(ggplot2)
library(tidyquant)
library(widyr)
library(igraph)
library(ggraph)

plotTimeSeries <- function(date_words,word){
    selected_word = date_words[which(date_words$word == word),]
    selected_words_by_month <- selected_word %>%
    tq_transmute(
    select = n,
    mutate_fun = apply.monthly,
    FUN = sum,
    na.rm = TRUE
    )
    for (ii in seq_along(selected_words_by_month$date)) {day(selected_words_by_month[ii,]$date) = 15}
    selected_words_by_month = merge(selected_words_by_month, total_adds_by_month, by = "date",all = TRUE)
    selected_words_by_month$n.x[is.na(selected_words_by_month$n.x)] = 0
    selected_words_by_month$ratio = selected_words_by_month$n.x / selected_words_by_month$n.y

    selected_words_by_month %>%
    ggplot(aes(x = date, y = ratio * 100)) +
        geom_point() +
        geom_smooth(method = "loess") +
        labs(title = paste("Use of the word",word,"by month"), x = "",
        y = "% of occurences") +
        theme(legend.position = "none")
    ggsave(paste("time_series_",word,".pdf",sep=''),device="pdf")
    #return (selected_words_by_month)
}

dataVector = read.csv("itjobs.csv", stringsAsFactors = FALSE, col.names = c("finnkode", "date", "text")) %>% mutate(date = ymd(date))

#Make tidytext
jobadds_tidy = dataVector %>% unnest_tokens(word, text, format = "html")

data(stop_words)
jobadds_tidy <- jobadds_tidy %>% anti_join(stop_words)
stop_words_no <- readLines("stopwords-no.txt")
stop_words_no <- data_frame(word = stop_words_no)
jobadds_tidy <- jobadds_tidy %>%
    anti_join(stop_words_no) %>%
    anti_join(stop_words_no)


date_words <- jobadds_tidy %>%
    count(date, word, sort = TRUE) %>%
    ungroup()

total_adds_by_date <- dataVector %>%
    count(date, sort = TRUE) %>%
    ungroup()
total_adds_by_month <- total_adds_by_date %>%
tq_transmute(
select = n,
mutate_fun = apply.monthly,
FUN = sum,
na.rm = TRUE)

for (ii in seq_along(total_adds_by_month$date)) {day(total_adds_by_month[ii,]$date) = 15}

plotTimeSeries(date_words,"java")

word_pairs <- jobadds_tidy %>%
pairwise_count(word, finnkode, sort = TRUE)

word_cors <- jobadds_tidy %>%
    group_by(word) %>%
    filter(n() >= 20) %>%
    pairwise_cor(word, finnkode, sort = TRUE)

word_cors %>%
    filter(item1 %in% c("java", "python", "sql", "cpp")) %>%
    group_by(item1) %>%
    top_n(6) %>%
    ungroup() %>%
    mutate(item2 = reorder(item2, correlation)) %>%
    ggplot(aes(item2, correlation)) +
    geom_bar(stat = "identity") +
    facet_wrap(~ item1, scales = "free") +
    coord_flip()
ggsave("languages_correlations.pdf",device="pdf")

word_cors %>%
    filter(item1 %in% c("steria","every")) %>%
    group_by(item1) %>%
    top_n(6) %>%
    ungroup() %>%
    mutate(item2 = reorder(item2, correlation)) %>%
    ggplot(aes(item2, correlation)) +
    geom_bar(stat = "identity") +
    facet_wrap(~ item1, scales = "free") +
    coord_flip()
