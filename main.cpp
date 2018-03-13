#include <iostream>
#include <vector>

struct Board
{
	char board[5][5];
	Board() {}
	Board(const Board& b) 
	{
		for(int i=0; i < 5; ++i)
		{
			for(int j=0; j < 5; ++j)
			{
				board[i][j] = b.board[i][j];
			}
		}
	}
	~Board() {}
};

struct BoardPosition
{
	int x;
	int y;
	BoardPosition(int _x, int _y) : x(_x), y(_y) {}
};

struct UCTResult
{
	int victory;
	int playout;
	BoardPosition play;
	std::vector<UCTResult*> next;
	UCTResult* previous=nullptr;
	float Evaluate() 
	{
		
	}
	UCTResult() {}
	UCTResult(BoardPosition pos) : play(pos) { }
	~UCTResult() {}
};

//char board[5][5];

void ClearBoard(Board& board)
{
	for(int i=0; i < 5; ++i)
	{
		for(int j=0; j < 5; ++j)
		{
			board.board[i][j] = ' ';
		}
	}
}

void DrawBoard(Board& board)
{
	for(int i=0; i < 5; ++i)
	{
		std::cout << "----";
	}
	std::cout << '-' << std::endl;
	for(int i=0; i < 5; ++i)
	{
		for(int j=0; j < 5; ++j)
		{
			std::cout << "| " << board.board[i][j] << " ";
		}
		std::cout << '|' << std::endl;
		for(int j=0; j < 5; ++j)
		{
			std::cout << "----";
		}
		std::cout << '-' << std::endl;
	}
}

//player 1 row
//player 2 col
bool CanPlay(int playerNumber, int col, int row, Board& board)
{
	if(board.board[col][row] != ' ')
		return false;

	if(playerNumber == 0)
	{

		if(row < 1 || col > 4)
			return false;

		if(board.board[col][row -1] != ' ')
			return false;
	}else{
		if(col > 3 || row > 4)
			return false;
		if(board.board[col+1][row] != ' ')
			return false;
	}
	return true;
}

std::vector<BoardPosition> ListPlayable(int playerNumber, Board& board)
{
	std::vector<BoardPosition> positions;
	for(int i=0; i < 5; ++i)
	{
		for(int j=0; j < 5; ++j)
		{
			if(CanPlay(playerNumber, i , j, board))
			{
				positions.push_back(BoardPosition(i,j));
			}
		}
	}
	return positions;
}

void PlayPosition(BoardPosition bp, int playerNumber, Board& board)
{
	if(playerNumber%2 == 1)
	{
		board.board[bp.x][bp.y] = 'o';
		board.board[bp.x+1][bp.y] = 'o';
	}else{
		board.board[bp.x][bp.y] = 'x';
		board.board[bp.x][bp.y-1] = 'x';
	}
}

//startingPlayer 0 si player 1 et 1 si player 2
bool SimulateGame(int startingPlayer, Board& board)
{
	srand(time(NULL));
	int i=startingPlayer;
	bool end = false;
	while(!end)
	{
		std::vector<BoardPosition> pos = ListPlayable(i%2, board);
		if(pos.size() == 0)
			end = true;
		else
		{
			BoardPosition bp = pos[rand() % pos.size()];
			PlayPosition(bp, i, board);
			++i;
		}
	}

	//si i%2 est celui qui a perdu
	//donc startingPlayer perd si i%2 == startingPlayer
	return i%2 != startingPlayer;
}

int MT_Simulate(Board& board, int playerNumber)
{
	int victory = 0;
	for(int i=0; i < 20; ++i)
	{
		if(SimulateGame(playerNumber, board))
		{
			//DrawBoard(board);
			++victory;
		}
	}

	return victory;
}

int MT_Explorate(Board board, int playerNumber, BoardPosition pos )
{
	PlayPosition(pos, playerNumber , board);
	return MT_Simulate(board, (playerNumber+1)%2);
}

void MonteCarlo()
{
	int currentPlayer = 0;
	Board board;
	ClearBoard(board);
	std::vector<BoardPosition> pos = ListPlayable(currentPlayer, board);
	UCTResult *rootNode, *selectedNode, *tmpNode; // what ?
	rootNode = new UCTResult();
	selectedNode = rootNode;
	//
	while(pos.size() > 0)
	{
		for(int i=0; i < pos.size(); ++i)
		{
			UCTResult *res = new UCTResult(pos[i]);
			int victory = MT_Explorate(board, currentPlayer, pos[i]);
			res->victory = victory;
			res->playout = 20;
			res->previous = selectedNode;
			selectedNode.next.push_back(res);
		}
		//Max
		float max = FLT_MAX, tmp;
		int maxIndex = -1;
		for(int i=0; i < selectedNode->next.size(), ++i)
		{
			tmp = selectedNode->next[i].victory / (float)selectedNode->next[i].playout;
			if(tmp > max)
			{
				max = tmp;
				maxIndex = i;
			}
		}
		
		int maxVic = selectedNode->next[maxIndex]->victory;
		int maxPlayout = selectedNode->next[maxIndex]->playout;
		tmpNode = selectedNode->next[maxIndex];
		while(tmpNode->previous != nullptr)
		{
			tmpNode = tmpNode->previous;
			tmpNode->playout += maxPlayout;
			tmpNode->victory += maxVic;
		}

		PlayPosition(pos[maxIndex], playerNumber , board);
		
		currentPlayer = (currentPlayer+1)%2;
		pos = ListPlayable(currentPlayer, board);
	}
}

int main()
{
	MonteCarlo();
	//getchar();
	return 0;
}

