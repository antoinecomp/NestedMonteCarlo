#!/usr/bin/env python3
from timeit import default_timer as timer

import csv 
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
plt.style.use("ggplot")

from keras.models import Sequential
from keras.layers import Dense

# we divide data.csv into train and tests 

with open("data.csv", 'r') as f:
    plays = np.array(list(csv.reader(f, delimiter=",")))
    print(plays.shape)    
# We take the 126 first columns as input
df = pd.DataFrame(data=plays[0:28961,1:256])
# We take the 126 last columns as output
Y = pd.DataFrame(data=plays[0:28961,129:256])

#plays.reshape((64,64))

#board = np.reshape(plays, (8, 8))

df['split'] = np.random.randn(df.shape[0], 1)
msk = np.random.rand(len(df)) <= 0.7

train_df = df[msk].fillna("sterby")
test_df = df[~msk].fillna("sterby")

# we take the 128 first columns has input
X_train = train_df.iloc[:,0:128].values
# we take the 128 last columns has input
y_train = train_df.iloc[:,129:].values
X_test = test_df.iloc[:,0:128].values
Y_test = test_df.iloc[:,129:].values

# Necesary Keras Importations

from keras.preprocessing import sequence
from keras.models import Model, Input
from keras.layers import Dense, Embedding, GlobalMaxPooling1D
from keras.preprocessing.text import Tokenizer
from keras.optimizers import Adam

# model construction

model = Sequential()
model.add(Dense(12, input_dim=128, activation='relu'))
model.add(Dense(128, activation='relu'))
model.add(Dense(128, activation='sigmoid'))

print("compile")
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

print("fit")
model.fit(X_test, Y_test, epochs=3, batch_size=10)

print("evaluate")

# evaluate the model
scores = model.evaluate(X_test, Y)
print("\n%s: %.2f%%" % (model.metrics_names[1], scores[1]*100))
