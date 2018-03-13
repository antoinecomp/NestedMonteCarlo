#include <iostream>
#include <vector>
#include <ctime>
#include <fstream>
#include <cstdlib>

struct Board
{
	char board[8][8];
	Board() {}
	Board(const Board& b) 
	{
		for(int i=0; i < 8; ++i)
		{
			for(int j=0; j < 8; ++j)
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
	BoardPosition() {}
	BoardPosition(int _x, int _y) : x(_x), y(_y) {}
};

struct UCTResult
{
	int victory;
	int playout;
	BoardPosition play;
	std::vector<UCTResult*> next;
	UCTResult* previous=nullptr;

	UCTResult() {}
	UCTResult(BoardPosition pos) : play(pos) { }
	~UCTResult() { 
		for (unsigned int i = 0; i < next.size(); ++i)
			delete next[i];
		next.clear();
	}
};

//char board[5][5];

void ClearBoard(Board& board)
{
	for(int i=0; i < 8; ++i)
	{
		for(int j=0; j < 8; ++j)
		{
			board.board[i][j] = ' ';
		}
	}
}

void DrawBoard(Board& board)
{
	for(int i=0; i < 8; ++i)
	{
		std::cout << "----";
	}
	std::cout << '-' << std::endl;
	for(int i=0; i < 8; ++i)
	{
		for(int j=0; j < 8; ++j)
		{
			std::cout << "| " << board.board[i][j] << " ";
		}
		std::cout << '|' << std::endl;
		for(int j=0; j < 8; ++j)
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

		if(row < 1 || col > 7)
			return false;

		if(board.board[col][row -1] != ' ')
			return false;
	}else{
		if(col > 6 || row > 7)
			return false;
		if(board.board[col+1][row] != ' ')
			return false;
	}
	return true;
}

int ListPlayable(int playerNumber, Board& board, BoardPosition* positions)
{
	//std::vector<BoardPosition> positions;
	int count = 0;
	for(int i=0; i < 8; ++i)
	{
		for(int j=0; j < 8; ++j)
		{
			if(CanPlay(playerNumber, i , j, board))
			{
				positions[count] = BoardPosition(i, j);
				//positions.push_back(BoardPosition(i,j));
				++count;
			}
		}
	}
	return count;
	//return positions;
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
	int size = 0;
	//MAx board position possible 8 * 8 = 64 
	BoardPosition* pos = new BoardPosition[80];
	while(!end)
	{
		//std::vector<BoardPosition> pos = ListPlayable(i%2, board);
		size = ListPlayable(i % 2, board, pos);
		if(size == 0)
			end = true;
		else
		{
			BoardPosition bp = pos[rand() % size];
			PlayPosition(bp, i, board);
			++i;
		}
	}
	delete pos;

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

std::ofstream csvFile;

void WriteRow(Board& board, BoardPosition selectedMove, int playerNumber)
{
	for (int i = 0; i < 8; ++i)
	{
		for (int j = 0; j < 8; ++j)
		{
			csvFile << (board.board[i][j] == ' ' ? "0" : "1") << ",";
		}
	}

	for (int i = 0; i < 8; ++i)
	{
		for (int j = 0; j < 8; ++j)
		{
			csvFile << (board.board[i][j] == ' ' ? "1" : "0") << ",";
		}
	}

	for (int i = 0; i < 8; ++i)
	{
		for (int j = 0; j < 8; ++j)
		{
			csvFile << playerNumber << ",";
		}
	}

	for (int i = 0; i < 8; ++i)
	{
		for (int j = 0; j < 8; ++j)
		{
			if (selectedMove.x == i && selectedMove.y == j)
			{
				csvFile << "1";
			}
			else {
				csvFile << "0";
			}

			if (i != 7 || j != 7)
				csvFile << ",";
		}
	}

	csvFile << std::endl;
}

void MonteCarlo()
{
	int currentPlayer = 0;
	Board board;
	ClearBoard(board);

	BoardPosition* pos = new BoardPosition[80];
	int size = ListPlayable(currentPlayer, board, pos);

	//std::vector<BoardPosition> pos = ListPlayable(currentPlayer, board);
	UCTResult *rootNode, *selectedNode, *tmpNode;
	rootNode = new UCTResult();
	selectedNode = rootNode;
	//
	while(size > 0)
	{
		for(unsigned int i=0; i < size; ++i)
		{
			UCTResult *res = new UCTResult(pos[i]);
			int victory = MT_Explorate(board, currentPlayer, pos[i]);
			res->victory = victory;
			res->playout = 20;
			res->previous = selectedNode;
			selectedNode->next.push_back(res);
		}
		//Max
		float max = -1.0f, tmp;
		int maxIndex = -1;
		for(unsigned int i=0; i < selectedNode->next.size(); ++i)
		{
			tmp = selectedNode->next[i]->victory / (float)selectedNode->next[i]->playout;
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

		tmpNode = selectedNode->next[maxIndex];
		BoardPosition play = selectedNode->next[maxIndex]->play;
		selectedNode = tmpNode;
		PlayPosition(play, currentPlayer, board);
		
		currentPlayer = (currentPlayer+1)%2;
		size = ListPlayable(currentPlayer, board, pos);

		WriteRow(board, play, currentPlayer);
	}

	delete pos;
	delete rootNode, selectedNode, tmpNode;
}

int main()
{
	csvFile.open("data.csv");
	for (int i = 0; i < 1000; ++i)
	{
		MonteCarlo();
	}
	//getchar();
	csvFile.close();
	return 0;
}

