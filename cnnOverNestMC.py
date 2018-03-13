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

import time, thread

class Board:

    """Class defining a Board characterized by :
    - an array"""


    

    def __init__(self): # Notre méthode constructeur

        """Constructeur de notre classe. Chaque attribut va être instancié

        avec une valeur par défaut... original"""
		board = [8][8];
		for(int i=0; i < 8; ++i):
			for(int j=0; j < 8; ++j):
				board[i][j] = 0;


def MonteCarlo():
	int currentPlayer = 0;
	# We gona make 100 random game to learn
	# then he plays, the other play random


if __name__ == "__main__":
    MonteCarlo();
