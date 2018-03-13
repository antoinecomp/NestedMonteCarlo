#!/usr/bin/env python3
from timeit import default_timer as timer


import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
plt.style.use("ggplot")
# import matplotlib # inline

domineering = pd.read_csv("~/NestedMonteCarlo/domineering.csv").fillna("sterby")

# May be useful then

from keras.preprocessing import sequence
from keras.models import Model, Input
from keras.layers import Dense, Embedding, GlobalMaxPooling1D
from keras.preprocessing.text import Tokenizer
from keras.optimizers import Adam

import time

class Board:

	"""Class defining a Board characterized by :
	- an array"""

	def __init__(self):

		"""Class Board constructor, define an array initialized with 0s"""
		board = [7][7]
		for i in range (0,7):
			for j in range (0,7):
				board[i][j] = 0

def listPlayable(currentPlayer, board):
	"""Method defining how many possibilites one player have"""
	# what vertical plays may the vertical player have
	if(currentPlayer==1):
		for i in range (0,7):
			# we shouldn't go above the last lines when testing the cases emptiness so we go until 7 for j
			for j in range (0,6):
				# we check if one position and the one below are empty
				if(board[i][j] == 0 and board[i][j+1] == 0):
					playable = {i,j}
					# we store his possible moves in his own list
					l.append(playable)
					print(currentPlayer + "possibilies" + l.length)
					return l
	# what horizontal plays may the horizontal player have
	else:
		# we shouldn't go above the last column when testing the cases emptiness so we go until 7 for i
		for i in range (0,6):
			for j in range (0,7):
				# we check if one position and the one below are empty
				if(board[i][j] == 0 and board[i+1][j] == 0):
					playable = {i,j}
					# we store his possible moves in his own list
					l.append(playable)
					print(currentPlayer + "possibilies" + l.length)
					return l


def MT_Explorate(board, playerNumber, move):
	PlayPosition(move, playerNumber, board)
	return MT_Simulate(board, (playerNumber+1)%2) # ???

def PlayPosition(boardPosition, playerNumber, board):
	if(playerNumber == 1):
		board[boardPosition.x][boardPosition.y] = '1'
		board[boardPosition.x+1][boardPosition.y] = '1'
	else:
		board[boardPositionboardPosition.x][bp.y] = '2'
		board[boardPosition.x][boardPosition.y+1] = '2'

################ Danger Zone ##################
class UCTResult:
	"""I don't understand what this Class defines. It has :
	- a number of victory
	- a number of playouts
	- a board position, I don't know what it is
	- a vector of UCTResults, I don't know what it is"""

	def __init__(self):

		"""Class UCTResult constructor, define an array initialized with 0s"""
		victory = 0
		playout = 0
		play = BoardPosition()
		# vector of UCTResults, I don't know what it corresponds in

	def __init__(self, position):

		"""Class UCTResult constructor, define an array initialized with 0s"""
		victory = 0
		playout = 0
		play = BoardPosition()
		# vector of UCTResults, I don't know what it corresponds in

class BoardPosition:
	"""Class defining the board positions, USELESS ?:
	- """

	def __init__(self):
		"""Class BoardPosition constructor"""
#################################################



def MonteCarlo():
	# player 0 plays vertically, player 1 horizontally
	currentPlayer = 1
	board = Board()
	playableMoves = listPlayable(1,board)
	print(playableMoves)
	for move in playableMoves: # shouldn't we .pop() them when they are no more useful ?
		res = UCTResult(move)
		victory = MT_Explorate(board, currentPlayer, move)
		
	# We gona make 100 random game to learn with Monte Carlo
	# then he plays with his knowledge, the other play random

#std::vector<BoardPosition> pos = ListPlayable(currentPlayer, board)


if __name__ == "__main__":
	domineering = pd.read_csv("~/NestedMonteCarlo/domineering.csv").fillna("sterby")
	for i from 0 to 1000:
		MonteCarlo()
    return 0
