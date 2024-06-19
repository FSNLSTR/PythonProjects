import math
from collections import defaultdict
import random
import matplotlib.pyplot as plt
import numpy as np
from sklearn.metrics import accuracy_score
from sklearn.model_selection import StratifiedKFold

# Reads in the data from pp1data/text files
def parse_text_file(file_path):
    data = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            parts = line.strip().split('\t')
            if len(parts) == 2:
                text = parts[0].strip()
                label = int(parts[1])
                data.append((text, label))
        return data

# Learn Naive Bayes Function
def learn_nb(training_data):
    class_counts = defaultdict(int)
    word_counts = defaultdict(lambda: defaultdict(int))

    for i in training_data:
        text = i[0]
        label = i[1]
        class_counts[label] += 1
        words = text.split()
        for word in words:
            word = word.replace(".", "")
            word = word.replace(",", "")
            word_counts[word][label] += 1
    return class_counts, word_counts

# Predict Naive Bayes Function
def predict_nb(test_data, class_counts, word_counts, m=0):
    total_documents = sum(class_counts.values())
    num_classes = len(class_counts)
    predicted = []

    for text in test_data:
        log_probs = {label: math.log(class_counts[label] / total_documents) for label in class_counts}
        words = text.split()

        for word in words:
            word = word.replace(".", "")
            word = word.replace(",", "")
            if word in word_counts:
                for class_label in class_counts:
                    word_given_class = (word_counts[word].get(class_label, 0) + m) / (
                                class_counts[class_label] + m * len(word_counts))
                    if word_given_class != 0:
                        log_probs[class_label] += math.log(word_given_class)

        predicted_label = max(log_probs, key=log_probs.get)
        predicted.append(predicted_label)

    return predicted


# Define a function to run stratified cross-validation and generate learning curves
def run_experiment_1(dataset_path, m_values, num_folds=10):
    data = parse_text_file(dataset_path)

    for m in m_values:
        print(f"Running experiment with m = {m} on dataset {dataset_path}...")

        # Initialize lists to store learning curve data
        train_sizes = []
        avg_accuracies = []
        std_accuracies = []

        # Perform stratified cross-validation
        for _ in range(num_folds):
            random.seed(42)

            # Shuffle the data to create random folds
            random.shuffle(data)

            fold_size = len(data) // num_folds
            for i in range(num_folds):
                test_data = data[i * fold_size: (i + 1) * fold_size]
                train_data = data[:i * fold_size] + data[(i + 1) * fold_size:]

                # Generate learning curve data
                train_set_sizes = [int(j / 10 * len(train_data)) for j in range(1, 11)]
                accuracies = []

                for size in train_set_sizes:
                    # Train Naive Bayes
                    subset_train_data = train_data[:size]
                    class_counts, word_counts = learn_nb(subset_train_data)

                    # Test Naive Bayes on the test_data
                    test_text = [item[0] for item in test_data]
                    true_labels = [item[1] for item in test_data]
                    predicted_labels = predict_nb(test_text, class_counts, word_counts, m)
                    accuracy = accuracy_score(true_labels, predicted_labels)
                    accuracies.append(accuracy)

                # Store the data for this fold
                train_sizes.append(train_set_sizes)
                avg_accuracies.append(accuracies)

        # Calculate average and standard deviation of accuracies for each train set size
        avg_accuracies = np.mean(avg_accuracies, axis=0)
        std_accuracies = np.std(avg_accuracies, axis=0)

        # Plot learning curve
        plt.errorbar(train_sizes[0], avg_accuracies, yerr=std_accuracies, label=f"m = {m}")

    plt.xlabel('Train Set Size')
    plt.ylabel('Average Accuracy')
    plt.title(f'Experiment #1: Learning Curves for {dataset_path}')
    plt.legend()
    plt.grid(True)
    plt.show()


# Run the experiment for each dataset and m value - Experiment 1
dataset_paths = ['imdb_labelled.txt', 'amazon_cells_labelled.txt', 'yelp_labelled.txt']
m_values = [0, 1]

for dataset_path in dataset_paths:
    run_experiment_1("pp1data/"+dataset_path, m_values)


def run_experiment_2(dataset_path, m_values, num_folds=10):
    data = parse_text_file(dataset_path)

    avg_accuracies = []
    std_accuracies = []

    for m in m_values:
        print(f"Running cross-validation with m = {m} on dataset {dataset_path}...")

        accuracies = []

        skf = StratifiedKFold(n_splits=num_folds, shuffle=True, random_state=42)

        for train_indices, test_indices in skf.split(data, [item[1] for item in data]):
            train_data = [data[i] for i in train_indices]
            test_data = [data[i] for i in test_indices]
            class_counts, word_counts = learn_nb(train_data)
            test_text = [item[0] for item in test_data]
            true_labels = [item[1] for item in test_data]
            predicted_labels = predict_nb(test_text, class_counts, word_counts, m)
            accuracy = accuracy_score(true_labels, predicted_labels)
            accuracies.append(accuracy)

        avg_accuracy = np.mean(accuracies)
        std_accuracy = np.std(accuracies)
        avg_accuracies.append(avg_accuracy)
        std_accuracies.append(std_accuracy)

    return avg_accuracies, std_accuracies


# From m = [0.1, 1]
m_values = [i * 0.1 for i in range(11)]
for dataset_path in dataset_paths:
    avg_accuracies, std_accuracies = run_experiment_2("pp1data/"+dataset_path, m_values)

    # Plot the results
    plt.errorbar(m_values, avg_accuracies, yerr=std_accuracies, marker='o', linestyle='-')
    plt.xlabel('Smoothing Parameter (m)')
    plt.ylabel('Average Accuracy')
    plt.title(f'Experiment #2: (Dataset: {dataset_path})')
    plt.grid(True)
    plt.show()

# From m = [1, 10]
m_values = list(range(1, 11))
for dataset_path in dataset_paths:
    avg_accuracies, std_accuracies = run_experiment_2("pp1data/"+dataset_path, m_values)

    # Plot the results
    plt.errorbar(m_values, avg_accuracies, yerr=std_accuracies, marker='o', linestyle='-')
    plt.xlabel('Smoothing Parameter (m)')
    plt.ylabel('Average Accuracy')
    plt.title(f'Experiment #2: (Dataset: {dataset_path})')
    plt.grid(True)
    plt.show()


