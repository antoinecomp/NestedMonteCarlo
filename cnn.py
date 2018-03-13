#!/usr/bin/env python3
	
import numpy as np
np.random.seed(123)  # for reproducibility

from keras.models import Sequential

from keras.layers import Dense, Dropout, Activation, Flatten

from keras.utils import np_utils

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

print y_train.shape
