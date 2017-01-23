This is an experiment implementing a very simple clustering algorithm.

## How it works
We have a `feeds.txt` file in the `resources` folder in the root of the project. This file contains links to the RSS feed of varios blogs. 

The `clb.rss-parse` namespace has a function called `word-count` which, given the URL of a feed returns a count of how many times given words appeard for that specific blog.

Applying this function to all the feeds we can have a count of the word-count of all blogs, from these we can derive a common wordlist (this is done in `clb.feed-process/get-wordlist`) which we then can use to normalize the word count of each blog. Once this is done we can think of the normalized word list as the 'dimensions' the space that this blogs inhabit has, and since all blogs specidy a value for every word we can easily place them around this space.

Based on this space analogy we find the 'distance' between each blog using pearson distance (`clb.clusters/pearson`). Iteratively we find the 2 nearest blogs, merge them into a new "blog" that as position has the average of the merged blogs' positions, and repeat until we are only left with one "blog". This process actually forms a tree of blogs.

## Next steps
Draw the resulting tree using a dendrogram