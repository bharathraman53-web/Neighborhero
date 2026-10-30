<?php
declare(strict_types=1);

namespace NeighborHero\Validators;

class Validator {
    private array $data;
    private array $errors = [];

    public function __construct(array $data) {
        $this->data = $data;
    }

    public static function make(array $data): self {
        return new self($data);
    }

    public function required(string $field, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (!isset($this->data[$field]) || trim((string)$this->data[$field]) === '') {
            $this->errors[$field][] = "{$label} is required.";
        }
        return $this;
    }

    public function email(string $field, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (!empty($this->data[$field])) {
            $val = trim((string)$this->data[$field]);
            if (!filter_var($val, FILTER_VALIDATE_EMAIL)) {
                $this->errors[$field][] = "{$label} must be a valid email address.";
            }
        }
        return $this;
    }

    public function phone10(string $field, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (!isset($this->data[$field]) || trim((string)$this->data[$field]) === '') {
            $this->errors[$field][] = "{$label} is required.";
            return $this;
        }

        $val = trim((string)$this->data[$field]);
        // Strictly 10 digits only: reject fewer, more, letters, symbols, spaces, or pasted junk
        if (!preg_match('/^\d{10}$/', $val)) {
            $this->errors[$field][] = "{$label} must be exactly 10 digits.";
        }
        return $this;
    }

    public function minLength(string $field, int $min, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (isset($this->data[$field]) && mb_strlen(trim((string)$this->data[$field])) < $min) {
            $this->errors[$field][] = "{$label} must have at least {$min} characters.";
        }
        return $this;
    }

    public function in(string $field, array $allowed, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (isset($this->data[$field]) && !in_array($this->data[$field], $allowed, true)) {
            $this->errors[$field][] = "{$label} must be one of: " . implode(', ', $allowed);
        }
        return $this;
    }

    public function integer(string $field, ?int $min = null, ?int $max = null, string $label = ''): self {
        $label = $label ?: ucfirst(str_replace('_', ' ', $field));
        if (isset($this->data[$field])) {
            $val = filter_var($this->data[$field], FILTER_VALIDATE_INT);
            if ($val === false) {
                $this->errors[$field][] = "{$label} must be an integer.";
            } else {
                if ($min !== null && $val < $min) {
                    $this->errors[$field][] = "{$label} must be at least {$min}.";
                }
                if ($max !== null && $val > $max) {
                    $this->errors[$field][] = "{$label} must be at most {$max}.";
                }
            }
        }
        return $this;
    }

    public function passes(): bool {
        return empty($this->errors);
    }

    public function errors(): array {
        return $this->errors;
    }

    public function firstError(): ?string {
        if (empty($this->errors)) {
            return null;
        }
        $first = reset($this->errors);
        return is_array($first) ? reset($first) : $first;
    }
}

