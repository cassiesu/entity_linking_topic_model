# entity_linking_topic_model

We designed and implemented an entity-linking system to link short and poorly composed search queries to corresponding Wikipedia pages. The system strives for enriching query contents as document for each query and utillizing web search results (contents of Wikipedia Pages) as candidate documents for all possible entities. 

After text pre-process, unsupervised LDA topic model is used to extract useful information through Gibbs sampling iterations. 

After the last iteration, all topics are represented as global word map probability distribution, all documents as topic statistic distribution. Then we sort the cos-similarity values between query document and entity documents of each query. Finally we apply rules for query-entity matching.
