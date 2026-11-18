# Rules

## Call layout

Wrap calls with two or more parenthesized arguments, including constructors and collection factories. A trailing lambda does not count as a parenthesized argument. Standalone UI calls in multiline Compose blocks also wrap a single argument; tiny inline slots may stay compact.

### KtLint

```kotlin
val statuses = listOf(
    Open,
    Closed,
)

FilterChip(
    selected = selected,
    onClick = onClick,
    label = { Text(text = title) },
)
```

### Disallowed

```kotlin
val statuses = listOf(Open, Closed)

FilterChip(selected = selected, onClick = onClick, label = { Text(text = title) })
```

Keep simple single-argument calls, short comparisons, conditions, argument values, and initializer receivers together when they fit. Comments and multiline argument expressions prevent compaction. Start a multiline `if` branch below its `when` arrow.

### KtLint

```kotlin
val title = stringResource(resource)
consume(
    title = stringResource(resource),
    options = listOf<Status?>(null) + Status.entries,
)
if (value == null) {
    return
}
when (value) {
    else ->
        if (enabled) {
            first()
        } else {
            second()
        }
}
```

### Disallowed

```kotlin
val title =
    stringResource(
        resource,
    )
consume(
    title =
        stringResource(resource),
    options =
        listOf<Status?>(null) + Status.entries,
)
if (value ==
    null
) {
    return
}
when (value) {
    else -> if (enabled) {
        first()
    } else {
        second()
    }
}
```

| Configuration setting | Default |
| --- | --- |
| `indent_size` | `4` |
| `max_line_length` | `140` |

Rule id: `matee:call-layout`

Suppress with:

```kotlin
@Suppress("ktlint:matee:call-layout")
```

Enable via `.editorconfig` (use `disabled` to disable):

```editorconfig
ktlint_matee_call-layout = enabled
```

## Compose layout

Inside `@Composable` functions and explicitly annotated composable lambdas, put each call in a method chain below its receiver. A single call on `Modifier` or `modifier` also wraps. Property references such as `Res.string.title` and single non-modifier calls such as `draft.copy(...)` stay together. Ordinary functions in the same file keep standard chain formatting; annotation aliases are supported.

Separate adjacent multiline UI calls and state/UI pairs with a blank line. UI calls are identified by an uppercase callee. Comments between statements retain their grouping.

Put the named `modifier` argument last inside the parentheses, before any trailing lambda. Incorrect argument order is reported without an automatic fix.

### KtLint

```kotlin
Text(
    text = title,
    modifier = Modifier
        .fillMaxWidth()
        .padding(space),
)

Text(
    text = names
        .filter { it.isNotBlank() }
        .joinToString(),
    modifier = modifier
        .weight(1f),
)
```

### Disallowed

```kotlin
Text(
    modifier = Modifier.fillMaxWidth().padding(space),
    text = title,
)
Text(
    text = names.filter { it.isNotBlank() }.joinToString(),
    modifier = modifier.weight(1f),
)
```

| Configuration setting | Default |
| --- | --- |
| `indent_size` | `4` |

Rule id: `matee:compose-layout`

Suppress with:

```kotlin
@Suppress("ktlint:matee:compose-layout")
```

Enable via `.editorconfig` (use `disabled` to disable):

```editorconfig
ktlint_matee_compose-layout = enabled
```
