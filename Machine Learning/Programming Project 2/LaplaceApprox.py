import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
from scipy.stats import norm, gamma

# Parameters
a = 1
b = 1
x = 3
lambda_values = np.linspace(0, 10, 1000)
# True posterior (Gamma distribution)
true_posterior = gamma.pdf(lambda_values, a + x, scale=1 / (b + 1))

# Laplace approximation
lambda_star = (a - 1 + x) / (b + 1)
variance = lambda_star**2 / (a - 1 + x)
laplace_approximation = norm.pdf(lambda_values, lambda_star, np.sqrt(variance))

# Plot the true posterior and Laplace approximation
plt.figure(figsize=(8, 6))
plt.plot(lambda_values, true_posterior, label='True Posterior (Gamma)')
plt.plot(lambda_values, laplace_approximation, label='Laplace Approximation (Normal)')
plt.xlabel('λ')
plt.ylabel('Density')
plt.title('True Posterior vs. Laplace Approximation')
plt.legend()
plt.grid()
plt.show()
