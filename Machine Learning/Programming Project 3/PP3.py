import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import pandas as pd
import time


def sigmoid(x):
    return 1 / (1 + np.exp(-x))

def glm(X, y, glm_type, alpha=10, max_iterations=100, tol=1e-3):
    m, n = X.shape
    w = np.zeros(n)  # Initialize weight vector
    num_iterations = 0

    for iteration in range(max_iterations):
        if glm_type == "Logistic":
            y_pred = sigmoid(X.dot(w))
            d = y - y_pred
            R = y_pred * (1 - y_pred)
        elif glm_type == "Poisson":
            y_pred = np.exp(X.dot(w))
            d = y - y_pred
            R = y_pred
        elif glm_type == "Ordinal":
            s = 1.5  # Ordinal regression parameter s
            phi = [-np.inf, -2, -1, 0, 1, np.inf]  # Ordinal regression thresholds
            K = len(phi)
            ai = X.dot(w)
            yi = np.zeros((m, K))

            for j in range(K):
                yi[:, j] = sigmoid(s * (phi[j] - ai))

            # Compute di and ri for each example i with label ti
            d = np.zeros(m)
            R = np.zeros(m)
            for i in range(m):
                ti = int(y[i])  # The ordinal label
                d[i] = yi[i, ti] + yi[i, ti - 1] - 1
                R[i] = (s ** 2) * ((yi[i, ti] * (1 - yi[i, ti])) + (yi[i, ti - 1] * (1 - yi[i, ti - 1])))

        H = -X.T.dot(R[:, np.newaxis] * X) - (alpha * np.eye(n))
        g = X.T.dot(d) - (alpha * w)

        # Update weights using Newton's method
        w_new = w - np.linalg.solve(H, g)

        # Check for convergence
        if np.linalg.norm(w_new - w) < tol:
            break

        w = w_new
        num_iterations += 1

    return w, num_iterations


def glm_output_run():
    files = {"Logistic": ["A", "usps"], "Poisson": ["AP"], "Ordinal": ["AO"]}

    for glm_type, datasets in files.items():
        for d in datasets:
            data = pd.read_csv('pp3data/' + d + '.csv', header=None)
            labels = pd.read_csv('pp3data/labels-' + d + '.csv', header=None)

            X = data.values
            y = labels.values.flatten()
            print("X-shape: " +str(X.shape))
            print("Y-shape: "+str(y.shape))
            runs = 30
            training_set_sizes = np.linspace(0.1, 1.0, 10)
            error_rates = np.zeros((runs, len(training_set_sizes)))
            iterations = []
            start_time = time.time()

            for run in range(runs):
                np.random.seed(run)
                permuted_indices = np.random.permutation(X.shape[0])
                test_size = X.shape[0] // 3
                test_indices = permuted_indices[:test_size]
                train_indices = permuted_indices[test_size:]

                X_train, y_train = X[train_indices], y[train_indices]
                X_test, y_test = X[test_indices], y[test_indices]

                # Initialize arrays to store error rates for different training set sizes
                run_error_rates = np.zeros(len(training_set_sizes))

                for i, train_size in enumerate(training_set_sizes):
                    train_size = int(train_size * len(X_train))
                    X_train_subset, y_train_subset = X_train[:train_size], y_train[:train_size]

                    # Train the logistic regression model
                    w_MAP, num_iterations = glm(X_train_subset, y_train_subset, glm_type)
                    iterations.append(num_iterations)

                    error = 0
                    if glm_type == "Logistic":
                        y_pred = sigmoid(X_test.dot(w_MAP))
                        error = (y_pred >= 0.5) != y_test

                    elif glm_type == "Poisson":
                        y_pred = np.exp(X_test.dot(w_MAP))
                        error = np.abs(y_pred - y_test)

                    elif glm_type == "Ordinal":
                        phi = [-np.inf, -2, -1, 0, 1, np.inf]
                        K = len(phi)
                        s = 1.5
                        a = X_test.dot(w_MAP.T)
                        yj = np.zeros((X_test.shape[0], K))

                        for j in range(K):
                            yj[:, j] = sigmoid(s * (phi[j] - a))

                        pj = np.zeros((X_test.shape[0], K))
                        pj[:, 0] = yj[:, 0]
                        for j in range(1, K):
                            pj[:, j] = yj[:, j] - yj[:, j - 1]

                        # Predict the ordinal labels tˆ
                        predicted_labels = np.argmax(pj, axis=1)

                        # Calculate the absolute error err
                        true_labels = y_test
                        error = np.abs(predicted_labels - true_labels)

                        # Calculate the average error rate
                    run_error_rates[i] = np.mean(error)

                error_rates[run] = run_error_rates

            end_time = time.time()
            # Calculate mean and standard deviation of error rates for each training set size
            mean_error_rates = np.mean(error_rates, axis=0)
            print("Mean error rates: "+str(mean_error_rates))
            std_dev_error_rates = np.std(error_rates, axis=0)

            print(glm_type + " Regression - " + d + " dataset")
            print("Average Iterations: "+str(np.mean(iterations)))
            print("Runtime: "+str(end_time - start_time))
            print("\n")

            # Plot the learning curves with error bars
            plt.errorbar(training_set_sizes, mean_error_rates, yerr=std_dev_error_rates, fmt='-o', capsize=5)
            plt.xlabel("Training Set Size")
            plt.ylabel("Classification Error Rate")
            plt.title(glm_type + " Regression Learning Curves with Error Bars - " + d + " dataset")
            plt.grid(True)
            plt.show()

glm_output_run()
