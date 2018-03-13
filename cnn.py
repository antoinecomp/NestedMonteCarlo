#!/usr/bin/env python3
from timeit import default_timer as timer


import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
plt.style.use("ggplot")

from keras.models import Sequential
from keras.layers import Dense

import csv

# we divide data.csv into train and tests 

with open("data.csv", 'r') as f:
wines = list(csv.reader(f, delimiter=";"))
print(wines[:3])

df['split'] = np.random.randn(df.shape[0], 1)
msk = np.random.rand(len(df)) <= 0.7

train_df = df[msk].fillna("sterby")
test_df = df[~msk].fillna("sterby")

# we take the 128 first columns has input
X_train = train_df.iloc[:,0:191].values
# we take the 128 last columns has input
y_train = train_df.iloc[:,192:].values
X_test = test_df.iloc[:,0:191].values
Y_test = test_df.iloc[:,192:].values

# Primero hacemos las importaciones necesarias de keras 

from keras.preprocessing import sequence
from keras.models import Model, Input
from keras.layers import Dense, Embedding, GlobalMaxPooling1D
from keras.preprocessing.text import Tokenizer
from keras.optimizers import Adam

model = Sequential()
model.add(Dense(12, input_dim=137, activation='relu'))
model.add(Dense(137, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

print "compile\n"
model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

print "fit\n"
model.fit(X_test, Y_test, epochs=3, batch_size=10)

# evaluate the model
scores = model.evaluate(X_test, Y)
print("\n%s: %.2f%%" % (model.metrics_names[1], scores[1]*100))

end = timer()
print "time in ms"
print(end - start)


# Generates output predictions for the input samples.
test_result =model.predict(x_test, batch_size=batch_size, verbose=1, steps=None)
print "test_result\n",test_result

# we put them in an array
array_result = np.array(test_result)




	# How do I apply a CNN over this csv file ?
		# What am I aiming at ?
		# What are the train and test ?
