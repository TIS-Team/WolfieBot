export function findPlayerRank(ranks, xp) {
    let currentRank = null;
    let nextRank = null;
    let previousRank = null;
    let currentIndex = -1;

    for (let i = 0; i < ranks.length; i++) {
        if (ranks[i].exp <= xp) {
            currentRank = ranks[i];
            currentIndex = i;
            previousRank = ranks[i - 1] || null;
        } else {
            nextRank = ranks[i];
            break;
        }
    }

    if (!currentRank) {
        currentRank = ranks[0];
        currentIndex = 0;
        nextRank = ranks[1] || null;
    }

    return { currentRank, nextRank, previousRank, currentIndex };
}
