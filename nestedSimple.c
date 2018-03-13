double playout (Board * board) {
  Move listeCoups [MaxLegalMoves];
  while (true) {
    int nb = board->legalMoves (listeCoups);
    if ((nb == 0) || board->terminal ())
      return board->score ();
    int n = rand () % nb;
    board->play (listeCoups [n]);
    if (board->length >= MaxPlayoutLength - 20) {
      return 0;
    }
  }
}
  
#include <float.h>

double bestScoreNested = -DBL_MAX;

int lengthBestRollout [10];
double scoreBestRollout [10];
Move bestRollout [10] [MaxPlayoutLength];

Board bestBoard;

double nested (Board & board, int n) {
  int nbMoves = 0;
  Move moves [MaxLegalMoves];

  lengthBestRollout [n] = -1;
  scoreBestRollout [n] = -DBL_MAX;
  float res;
  while (true) {
    if (board.terminal ())
      return 0.0;
      //return board.score ();
    nbMoves = board.legalMoves (moves);
    for (int i = 0; i < nbMoves; i++) {
      Board b = board;
      b.play (moves [i]);
      if (n == 1)
	playout (&b);
      else
	nested (b, n - 1);     
      double score = b.score ();
      if (score > scoreBestRollout [n]) {
	scoreBestRollout [n] = score;
	lengthBestRollout [n] = b.length;
	for (int k = 0; k < b.length; k++)
	  bestRollout [n] [k] = b.rollout [k];
	if (n > 3) {
	  for (int t = 0; t < n - 1; t++)
	    fprintf (stderr, "\t");
	  fprintf (stderr, "n = %d, progress = %d, score = %f\n", n, board.length, scoreBestRollout [n]);
          int depth = 0;
          b.print (stderr);
	  fprintf (stderr, "\n");
	}
	if ((n > 1) && (score > bestScoreNested)) {
	  bestScoreNested = score;
	  fprintf (stderr, "best score = %f\n", score);
          b.print (stderr);
	  fprintf (stderr, "\n");
          bestBoard = b;
        }
      }
    }
    board.play (bestRollout [n] [board.length]);
  }
  return 0.0;
}

