
library(dplyr)
library(tidytext)
library(tidyr)
library(ggplot2)
dataVector=readLines("addtexts.txt")

#Make tidytext
text_df <- data_frame(line= 1:length(dataVector) ,text = dataVector)
jobadds_tidy = text_df %>% unnest_tokens(word, text)

data(stop_words)
jobadds_tidy <- jobadds_tidy %>% anti_join(stop_words)
stop_words_no <- readLines("stopwords-no.txt")
stop_words_no <- data_frame(word = stop_words_no)
jobadds_tidy <- jobadds_tidy %>% anti_join(stop_words_no) %>% anti_join(stop_words_no)

#plot word frequency
png(file="word_frequency.png",width = 600, height = 600, units = "px",pointsize=24)
jobadds_tidy %>%
  count(word, sort = TRUE) %>%
  filter(n > 100) %>%
  mutate(word = reorder(word, n)) %>%
  ggplot(aes(word, n)) +
  geom_col() +
  xlab(NULL) +
  coord_flip() +
    theme(axis.text=element_text(size=18))
dev.off()


  job_words <- jobadds_tidy %>%
    count(line, word, sort = TRUE) %>%
    ungroup()

  job_words

#plot tf_idf
  plot_words <- job_words %>%
    bind_tf_idf(word, line, n) %>%
    arrange(desc(tf_idf)) %>%
    mutate(word = factor(word, levels = rev(unique(word)))) %>%
    mutate(line = factor(line, levels = 1:length(dataVector)))

  plot_words %>%
    filter(line %in% c(111,112,113,114,115,116)) %>%
    group_by(line) %>%
    top_n(5, tf_idf) %>%
    ungroup() %>%
    mutate(word = reorder(word, tf_idf)) %>%
    ggplot(aes(word, tf_idf, fill = line)) +
    geom_col(show.legend = FALSE) +
    labs(x = NULL, y = "tf-idf") +
    facet_wrap(~line, ncol = 2, scales = "free") +
    coord_flip()

#bigrams
job_bigrams = text_df %>%
unnest_tokens(bigram, text, token = "ngrams", n = 2)
bigrams_separated <- job_bigrams %>%
separate(bigram, c("word1", "word2"), sep = " ")
bigrams_filtered <- bigrams_separated %>%
    filter(!word1 %in% stop_words$word) %>%
    filter(!word2 %in% stop_words$word) %>%
    filter(!word1 %in% stop_words_no$word) %>%
    filter(!word2 %in% stop_words_no$word)

bigrams_united <- bigrams_filtered %>%
unite(bigram, word1, word2, sep = " ")


#plot bigram frequency
png(file="bigram_frequency.png",width = 600, height = 600, units = "px",pointsize=24)
bigrams_united %>%
    count(bigram, sort = TRUE) %>%
    filter(n > 20) %>%
    mutate(bigram = reorder(bigram, n)) %>%
    ggplot(aes(bigram, n)) +
    geom_col() +
    xlab(NULL) +
    coord_flip() +
    theme(axis.text=element_text(size=18))
dev.off()

bigram_tf_idf <- bigrams_united %>%
    count(line, bigram) %>%
    bind_tf_idf(bigram, line, n) %>%
    arrange(desc(tf_idf))

#plot bigram tf_idf
plot_bigrams <- bigram_tf_idf %>%
    bind_tf_idf(bigram, line, n) %>%
    arrange(desc(tf_idf)) %>%
    mutate(bigram = factor(bigram, levels = rev(unique(bigram)))) %>%
    mutate(line = factor(line, levels = 1:length(dataVector)))

plot_bigrams %>%
    filter(line %in% c(111,112,113,114,115,116)) %>%
    group_by(line) %>%
    top_n(5, tf_idf) %>%
    ungroup() %>%
    mutate(bigram = reorder(bigram, tf_idf)) %>%
    ggplot(aes(bigram, tf_idf, fill = line)) +
    geom_col(show.legend = FALSE) +
    labs(x = NULL, y = "tf-idf") +
    facet_wrap(~line, ncol = 2, scales = "free") +
    coord_flip()