import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import pandas as pd
from sklearn.model_selection import KFold
import time

datasets = ['artlarge', 'artsmall', 'crime', 'wine']

# Defines a range of regularization parameters (λ)
lambda_values = np.arange(0, 151, 1)

# Create subplots
fig, axs = plt.subplots(2, 2, figsize=(12, 10))
fig.tight_layout(pad=5.0)  # Add padding between subplots

def ridge_regression(X, y, lambda_val):
    N = X.shape[0]
    M = X.shape[1]

    # Calculate ΦᵀΦ
    phi_phi_transpose = np.dot(X.T, X)

    # Calculate λI
    lambda_I = lambda_val * np.identity(M)

    # Calculate (λI + ΦᵀΦ)⁻¹
    inv_term = np.linalg.inv(np.add(lambda_I, phi_phi_transpose))

    # Calculate Φᵀt
    phi_transpose_y = np.dot(X.T, y)

    # Calculate w using the equation: w = (λI + ΦᵀΦ)⁻¹Φᵀt
    w = np.dot(inv_term, phi_transpose_y)

    # Calculate predictions
    y_pred = np.dot(X, w)

    # Calculate Mean Squared Error (MSE)
    mse = np.sum((y - y_pred) ** 2) / N

    return w, mse

#Task 1: Regularization
print("Task 1:")
for i, dataset in enumerate(datasets):
    # Divide subplots into rows and columns
    row = i // 2
    col = i % 2
    ax = axs[row, col]

    train_data = pd.read_csv('pp2data/train-'+dataset+'.csv', header=None)
    train_labels = pd.read_csv('pp2data/trainR-'+dataset+'.csv', header=None)
    test_data = pd.read_csv('pp2data/test-'+dataset+'.csv', header=None)
    test_labels = pd.read_csv('pp2data/testR-'+dataset+'.csv', header=None)


    X_train = train_data.values
    y_train = train_labels.values.ravel()
    X_test = test_data.values
    y_test = test_labels.values.ravel()


    # Lists to store MSE values
    train_mse = []
    test_mse = []

    # Iterate through lambda values and fit Ridge regression models
    for lambda_val in lambda_values:
        # Fit the Ridge regression model
        w, train_mse_val = ridge_regression(X_train, y_train, lambda_val)

        # Predict on training and test data
        y_test_pred = np.dot(X_test, w)

        # Calculate test set MSE
        test_mse_val = np.sum((y_test_pred - y_test) ** 2) / len(y_test)

        train_mse.append(train_mse_val)
        test_mse.append(test_mse_val)

    # Plot the results on the current subplot
    ax.plot(lambda_values, train_mse, label="Training MSE")
    ax.plot(lambda_values, test_mse, label="Test MSE")
    if dataset == 'artsmall':
        ax.axhline(y=0.533, color='r', linestyle='--', label="True Function MSE")
    elif dataset == 'artlarge':
        ax.axhline(y=0.557, color='r', linestyle='--', label="True Function MSE")
    ax.set_xlabel("Regularization Parameter (λ)")
    ax.set_ylabel("Mean Squared Error (MSE)")
    ax.set_title("Regularization in Linear Regression - "+dataset+" Dataset")
    ax.legend()
    ax.grid(True)


    print(f"Dataset: {dataset}")
    minMSElambda = test_mse.index(min(test_mse))
    print("Lambda:"+str(minMSElambda))
    print("Min MSE:"+str(min(test_mse)))
    print("\n")

# Show the entire figure with subplots
plt.show()


#Task 2

# Initialize variables to store results
best_lambda = {}  # Dictionary to store the best λ for each dataset
best_avg_validation_mse = {}  # Dictionary to store the best average validation MSE for each dataset
test_mse = {}  # Dictionary to store the test MSE for each dataset
runtime = {}

for i, dataset in enumerate(datasets):
    train_data = pd.read_csv('pp2data/train-'+dataset+'.csv', header=None)
    train_labels = pd.read_csv('pp2data/trainR-'+dataset+'.csv', header=None)
    test_data = pd.read_csv('pp2data/test-'+dataset+'.csv', header=None)
    test_labels = pd.read_csv('pp2data/testR-'+dataset+'.csv', header=None)

    X_train = train_data.values
    y_train = train_labels.values.ravel()
    X_test = test_data.values
    y_test = test_labels.values.ravel()

    # Perform 10-fold cross-validation for λ selection
    kf = KFold(n_splits=10, shuffle=True, random_state=42)

    best_lambda[dataset] = None
    best_avg_validation_mse[dataset] = float('inf')
    test_mse[dataset] = None
    start_time = time.time()

    for lambda_val in lambda_values:
        validation_mses = []

        for train_index, val_index in kf.split(X_train):
            X_fold_train, X_fold_val = X_train[train_index], X_train[val_index]
            y_fold_train, y_fold_val = y_train[train_index], y_train[val_index]

            w, train_mse_val = ridge_regression(X_fold_train, y_fold_train, lambda_val)

            # Predict on training and test data
            y_val_pred = np.dot(X_fold_val, w)

            # Calculate test set MSE
            validation_mse = np.sum((y_fold_val - y_val_pred) ** 2) / len(y_fold_val)
            validation_mses.append(validation_mse)

        avg_validation_mse = np.mean(validation_mses)

        # Check if this λ has the lowest average validation MSE
        if avg_validation_mse < best_avg_validation_mse[dataset]:
            best_avg_validation_mse[dataset] = avg_validation_mse
            best_lambda[dataset] = lambda_val

            # Retrain the model on the entire training set with the best lambda
            w, test_mse_val = ridge_regression(X_train, y_train, best_lambda[dataset])

            # Evaluate the model on the test set
            y_test_pred = np.dot(X_test, w)

            # Calculate test set MSE
            validation_mse = np.sum((y_test - y_test_pred) ** 2) / len(y_test)
            test_mse[dataset] = validation_mse

    end_time = time.time()
    runtime[dataset] = end_time - start_time

print("Task 2")
# Report results for all datasets
for dataset in datasets:
    print(f"Dataset: {dataset}")
    print(f"Selected λ: {best_lambda[dataset]}")
    print(f"Validation MSE with selected λ: {best_avg_validation_mse[dataset]}")
    print(f"Test MSE with selected λ: {test_mse[dataset]}")
    print(f"Runtime: {runtime[dataset]} seconds")
    print("\n")

print("\nTask 3")
# Task 3: Bayesian Model Selection
for i, dataset in enumerate(datasets):
    train_data = pd.read_csv('pp2data/train-'+dataset+'.csv', header=None)
    train_labels = pd.read_csv('pp2data/trainR-'+dataset+'.csv', header=None)
    test_data = pd.read_csv('pp2data/test-'+dataset+'.csv', header=None)
    test_labels = pd.read_csv('pp2data/testR-'+dataset+'.csv', header=None)

    X_train = train_data.values
    y_train = train_labels.values.ravel()
    X_test = test_data.values
    y_test = test_labels.values.ravel()

    # Initialize alpha (α) and beta (β) to random values in the range [1, 10]
    alpha = np.random.uniform(1, 10)
    beta = np.random.uniform(1, 10)

    # Define convergence threshold
    threshold = 0.0001

    # Initialize variables to store results
    effective_lambda = None
    mse = None

    start_time = time.time()

    N = X_train.shape[0]
    M = X_train.shape[1]

    # Iterative algorithm to select α and β
    while True:
        lambda_values = np.linalg.eigvals(beta * np.dot(X_train.T, X_train))
        # Calculate SN and mN using equations (3.53) and (3.54)
        SN = np.linalg.inv((alpha * np.identity(M)) + (beta * np.dot(X_train.T, X_train)))
        mN = beta * np.dot(np.dot(SN, X_train.T), y_train)

        # Update alpha and beta using equations (3.91) and (3.92)

        gamma = np.sum(lambda_values / (alpha + lambda_values))

        alpha_new = gamma / np.dot(mN.T, mN)

        y_pred = np.dot(X_train, mN)
        beta_new = (N - gamma)/(np.sum((y_train - y_pred) ** 2))

        # Check for convergence
        if abs(alpha_new - alpha) < threshold and abs(beta_new - beta) < threshold:
            break

        alpha = alpha_new
        beta = beta_new

    # Calculate effective λ as α/β
    effective_lambda = alpha / beta

    mN = beta * np.dot(np.dot(SN, X_train.T), y_train)
    # Use mN for prediction on the test set
    y_test_pred = np.dot(X_test, mN)

    # Calculate test set MSE
    mse = np.sum((y_test - y_test_pred) ** 2) / len(y_test)

    end_time = time.time()

    # Report results
    print(f"Dataset: {dataset}")
    print(f"Selected α: {alpha}")
    print(f"Selected β: {beta}")
    print(f"Effective λ (α/β): {effective_lambda}")
    print(f"Test MSE with selected α and β: {mse}")
    print(f"Runtime: {end_time - start_time} seconds")
    print('\n')

